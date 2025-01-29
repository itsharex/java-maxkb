package com.litongjava.maxkb.service;

import java.util.ArrayList;
import java.util.List;

import com.jfinal.kit.Kv;
import com.litongjava.db.TableInput;
import com.litongjava.db.TableResult;
import com.litongjava.db.activerecord.Db;
import com.litongjava.db.activerecord.Row;
import com.litongjava.jfinal.aop.Aop;
import com.litongjava.maxkb.constant.TableNames;
import com.litongjava.maxkb.service.kb.MaxKbEmbeddingService;
import com.litongjava.maxkb.service.kb.MaxKbSentenceService;
import com.litongjava.maxkb.vo.DocumentBatchVo;
import com.litongjava.maxkb.vo.Paragraph;
import com.litongjava.model.result.ResultVo;
import com.litongjava.table.services.ApiTable;
import com.litongjava.tio.utils.crypto.Md5Utils;
import com.litongjava.tio.utils.hutool.FilenameUtils;
import com.litongjava.tio.utils.snowflake.SnowflakeIdUtils;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public class DatasetDocumentVectorService {

  MaxKbEmbeddingService maxKbEmbeddingService = Aop.get(MaxKbEmbeddingService.class);
  MaxKbSentenceService maxKbSentenceService = Aop.get(MaxKbSentenceService.class);

  public ResultVo batch(Long userId, Long dataset_id, List<DocumentBatchVo> list) {

    TableInput tableInput = new TableInput();
    tableInput.set("id", dataset_id);
    if (!userId.equals(1L)) {
      tableInput.set("user_id", userId);
    }

    TableResult<Row> result = ApiTable.get(TableNames.max_kb_dataset, tableInput);

    Row dataset = result.getData();
    if (dataset == null) {
      return ResultVo.fail("Dataset not found.");
    }

    Long embedding_mode_id = dataset.getLong("embedding_mode_id");
    String sqlModelName = String.format("SELECT model_name FROM %s WHERE id = ?", TableNames.max_kb_model);
    String modelName = Db.queryStr(sqlModelName, embedding_mode_id);
    String sqlDocumentId = String.format("SELECT id FROM %s WHERE user_id = ? AND file_id = ?", TableNames.max_kb_document);

    List<Kv> kvs = new ArrayList<>();

    for (DocumentBatchVo documentBatchVo : list) {
      Long fileId = documentBatchVo.getId();
      String filename = documentBatchVo.getName();
      Long documentId = Db.queryLong(sqlDocumentId, userId, fileId);

      List<Paragraph> paragraphs = documentBatchVo.getParagraphs();
      int char_length = 0;
      int size = 0;
      if (paragraphs != null) {
        size = paragraphs.size();
        for (Paragraph p : paragraphs) {
          if (p.getContent() != null) {
            char_length += p.getContent().length();
          }
        }
      }

      String type = FilenameUtils.getSuffix(filename);

      if (documentId == null) {
        documentId = SnowflakeIdUtils.id();
        Row record = Row.by("id", documentId)
            //
            .set("file_id", fileId).set("user_id", userId).set("name", filename)
            //
            .set("char_length", char_length).set("status", "1").set("is_active", true)
            //
            .set("type", type).set("dataset_id", dataset_id).set("paragraph_count", size)
            //
            .set("hit_handling_method", "optimization").set("directly_return_similarity", 0.9);
        Db.save(TableNames.max_kb_document, record);
        Kv kv = record.toKv();
        kvs.add(kv);
      } else {
        Row existingRecord = Db.findById(TableNames.max_kb_document, documentId);
        if (existingRecord != null) {
          Kv kv = existingRecord.toKv();
          kvs.add(kv);
        } else {
          // Handle the case where documentId is provided but the record does not exist
          return ResultVo.fail("Document not found for ID: " + documentId);
        }
      }

      List<Row> paragraphRecords = new ArrayList<>();
      Long documentIdFinal = documentId;
      boolean transactionSuccess = saveToParagraph(dataset_id, fileId, documentId, paragraphs, type, paragraphRecords);

      if (!transactionSuccess) {
        return ResultVo.fail("Transaction failed while saving paragraphs for document ID: " + documentIdFinal);
      }

      transactionSuccess = Aop.get(MaxKbSentenceService.class).summaryToSentenceAndSave(dataset_id, modelName, paragraphRecords, documentIdFinal);

      if (!transactionSuccess) {
        return ResultVo.fail("Transaction failed while summary paragraph for document ID: " + documentIdFinal);
      }

      transactionSuccess = maxKbSentenceService.splitToSentenceAndSave(dataset_id, modelName, paragraphRecords, documentIdFinal);

      if (!transactionSuccess) {
        return ResultVo.fail("Transaction failed while saving senttents for document ID: " + documentIdFinal);
      }

    }

    return ResultVo.ok(kvs);
  }

  private boolean saveToParagraph(Long dataset_id, Long fileId, Long documentId, List<Paragraph> paragraphs,
      //
      String type, List<Row> batchRecord) {
    final long documentIdFinal = documentId;
    if (paragraphs != null) {
      for (Paragraph p : paragraphs) {

        String title = p.getTitle();
        String content = p.getContent();

        Row row = Row.by("id", SnowflakeIdUtils.id())
            //
            .set("source_id", fileId)
            //
            .set("source_type", type)
            //
            .set("title", title)
            //
            .set("content", content)
            //
            .set("md5", Md5Utils.getMD5(content))
            //
            .set("status", "1")
            //
            .set("hit_num", 0)
            //
            .set("is_active", true).set("dataset_id", dataset_id).set("document_id", documentIdFinal);
        batchRecord.add(row);

      }
    }
    return Db.tx(() -> {
      Db.delete(TableNames.max_kb_paragraph, Row.by("document_id", documentIdFinal));
      Db.batchSave(TableNames.max_kb_paragraph, batchRecord, 2000);
      return true;
    });
  }
}
