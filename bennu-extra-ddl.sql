
CREATE FUNCTION jsonv_str(json LONGVARCHAR, path varchar(32767)) RETURNS LONGVARCHAR
   LANGUAGE JAVA DETERMINISTIC NO SQL
   EXTERNAL NAME 'CLASSPATH:com.qoid.bennu.util.HsqldbAssist.json_str'

;;;

CREATE FUNCTION jsonv_bool(json LONGVARCHAR, path varchar(32767)) RETURNS BOOLEAN
   LANGUAGE JAVA DETERMINISTIC NO SQL
   EXTERNAL NAME 'CLASSPATH:com.qoid.bennu.util.HsqldbAssist.json_bool'

;;;

CREATE FUNCTION jsonv_int(json LONGVARCHAR, path varchar(32767)) RETURNS INTEGER
   LANGUAGE JAVA DETERMINISTIC NO SQL
   EXTERNAL NAME 'CLASSPATH:com.qoid.bennu.util.HsqldbAssist.json_int'

;;;

CREATE FUNCTION json_str (json CLOB, path varchar(32767)) RETURNS LONGVARCHAR
   RETURN jsonv_str(cast(json as LONGVARCHAR), path)

;;;

CREATE FUNCTION json_bool (json CLOB, path varchar(32767)) RETURNS LONGVARCHAR
   RETURN jsonv_bool(cast(json as LONGVARCHAR), path)

;;;

CREATE FUNCTION json_int (json CLOB, path varchar(32767)) RETURNS LONGVARCHAR
   RETURN jsonv_int(cast(json as LONGVARCHAR), path)

;;;





