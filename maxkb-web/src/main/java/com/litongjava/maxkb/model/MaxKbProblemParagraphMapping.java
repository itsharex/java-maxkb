package com.litongjava.maxkb.model;

import com.litongjava.maxkb.model.base.BaseMaxKbProblemParagraphMapping;

/**
 * Generated by java-db.
 */
public class MaxKbProblemParagraphMapping extends BaseMaxKbProblemParagraphMapping<MaxKbProblemParagraphMapping> {
  private static final long serialVersionUID = 1L;
	public static final MaxKbProblemParagraphMapping dao = new MaxKbProblemParagraphMapping().dao();
	/**
	 * 
	 */
  public static final String tableName = "max_kb_problem_paragraph_mapping";
  public static final String primaryKey = "id";
  //java.lang.Long 
  public static final String id = "id";
  //java.lang.Long 
  public static final String datasetId = "dataset_id";
  //java.lang.Long 
  public static final String documentId = "document_id";
  //java.lang.Long 
  public static final String paragraphId = "paragraph_id";
  //java.lang.Long 
  public static final String problemId = "problem_id";
  //java.lang.String 
  public static final String creator = "creator";
  //java.util.Date 
  public static final String createTime = "create_time";
  //java.lang.String 
  public static final String updater = "updater";
  //java.util.Date 
  public static final String updateTime = "update_time";
  //java.lang.Integer 
  public static final String deleted = "deleted";
  //java.lang.Long 
  public static final String tenantId = "tenant_id";

  @Override
  protected String _getPrimaryKey() {
    return primaryKey;
  }

  @Override
  protected String _getTableName() {
    return tableName;
  }
}

