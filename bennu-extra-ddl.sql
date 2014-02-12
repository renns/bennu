

CREATE FUNCTION json_str(json CLOB, path varchar(250)) RETURNS varchar(32767)
   LANGUAGE JAVA DETERMINISTIC NO SQL
   EXTERNAL NAME 'CLASSPATH:com.qoid.bennu.util.HsqldbAssist.json_str'


;;;


CREATE FUNCTION json_str(json varchar(32767), path varchar(250)) RETURNS varchar(32767)
   LANGUAGE JAVA DETERMINISTIC NO SQL
   EXTERNAL NAME 'CLASSPATH:com.qoid.bennu.util.HsqldbAssist.json_str'

