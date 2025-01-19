package com.litongjava.maxkb.model;

import com.litongjava.maxkb.model.base.BaseMaxKbUser;

/**
 * Generated by java-db.
 */
public class MaxKbUser extends BaseMaxKbUser<MaxKbUser> {
  private static final long serialVersionUID = 1L;
	public static final MaxKbUser dao = new MaxKbUser().dao();
	/**
	 * 
	 */
  public static final String tableName = "max_kb_user";
  public static final String primaryKey = "id";
  //java.lang.Long 
  public static final String id = "id";
  //java.lang.String 
  public static final String email = "email";
  //java.lang.String 
  public static final String phone = "phone";
  //java.lang.String 
  public static final String nickName = "nick_name";
  //java.lang.String 
  public static final String username = "username";
  //java.lang.String 
  public static final String password = "password";
  //java.lang.String 
  public static final String role = "role";
  //java.lang.Boolean 
  public static final String isActive = "is_active";
  //java.lang.String 
  public static final String source = "source";
  //java.lang.String 
  public static final String remark = "remark";
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

