
create cached table agent (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	uberAliasIid varchar(32) not NULL,
	name varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
create cached table alias (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	rootLabelIid varchar(32) not NULL,
	name varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
create cached table connection (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	metaLabelIid varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	localPeerId varchar(32) not NULL,
	remotePeerId varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
create cached table content (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	contentType varchar(32) not NULL,
	metaData Clob not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
create cached table introduction (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	aConnectionIid varchar(32) not NULL,
	aState varchar(50) not NULL,
	bConnectionIid varchar(32) not NULL,
	bState varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
create cached table label (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	name varchar(50) not NULL,
	icon varchar(50),
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
create cached table labelAcl (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	connectionIid varchar(32) not NULL,
	labelIid varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
create cached table labelchild (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	parentIid varchar(32) not NULL,
	childIid varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
CREATE UNIQUE INDEX labelchild_autogen_parent_child ON labelchild (parentIid,childIid)
;
create cached table labeledContent (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	contentIid varchar(32) not NULL,
	labelIid varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
CREATE UNIQUE INDEX labeledContent_autogen_contentLabel ON labeledContent (contentIid,labelIid)
;
create cached table login (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	authenticationId varchar(100) not NULL,
	passwordHash char(60) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
create cached table notification (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	consumed bit not NULL,
	fromConnectionIid varchar(32) not NULL,
	kind varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
create cached table profile (
	iid varchar(32) not NULL primary key,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	name varchar(50) not NULL,
	imgSrc Clob not NULL,
	sharedId varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL
)
;
create cached table agent_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	uberAliasIid varchar(32) not NULL,
	name varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
create cached table alias_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	rootLabelIid varchar(32) not NULL,
	name varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
create cached table connection_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	metaLabelIid varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	localPeerId varchar(32) not NULL,
	remotePeerId varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
create cached table content_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	contentType varchar(32) not NULL,
	metaData Clob not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
create cached table introduction_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aConnectionIid varchar(32) not NULL,
	aState varchar(50) not NULL,
	bConnectionIid varchar(32) not NULL,
	bState varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
create cached table labelAcl_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	connectionIid varchar(32) not NULL,
	labelIid varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
create cached table label_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	name varchar(50) not NULL,
	icon varchar(50),
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
create cached table labelchild_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	parentIid varchar(32) not NULL,
	childIid varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
create cached table labeledContent_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	contentIid varchar(32) not NULL,
	labelIid varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
create cached table login_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	authenticationId varchar(100) not NULL,
	passwordHash char(60) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
create cached table notification_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	consumed bit not NULL,
	fromConnectionIid varchar(32) not NULL,
	kind varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
create cached table profile_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	name varchar(50) not NULL,
	imgSrc Clob not NULL,
	sharedId varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigint generated by default as identity (start with 1) not NULL primary key,
	auditAction char(6) not NULL
)
;
CREATE TRIGGER login_log_insert AFTER INSERT ON login
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO login_log
(
	iid,
	agentId,
	aliasIid,
	authenticationId,
	passwordHash,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.aliasIid,
	NEW.authenticationId,
	NEW.passwordHash,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER login_log_update AFTER UPDATE ON login
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO login_log
(
	iid,
	agentId,
	aliasIid,
	authenticationId,
	passwordHash,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.aliasIid,
	NEW.authenticationId,
	NEW.passwordHash,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER login_log_delete AFTER DELETE ON login
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO login_log
(
	iid,
	agentId,
	aliasIid,
	authenticationId,
	passwordHash,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.aliasIid,
	OLD.authenticationId,
	OLD.passwordHash,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)
;
CREATE TRIGGER connection_log_insert AFTER INSERT ON connection
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO connection_log
(
	iid,
	agentId,
	metaLabelIid,
	aliasIid,
	localPeerId,
	remotePeerId,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.metaLabelIid,
	NEW.aliasIid,
	NEW.localPeerId,
	NEW.remotePeerId,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER connection_log_update AFTER UPDATE ON connection
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO connection_log
(
	iid,
	agentId,
	metaLabelIid,
	aliasIid,
	localPeerId,
	remotePeerId,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.metaLabelIid,
	NEW.aliasIid,
	NEW.localPeerId,
	NEW.remotePeerId,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER connection_log_delete AFTER DELETE ON connection
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO connection_log
(
	iid,
	agentId,
	metaLabelIid,
	aliasIid,
	localPeerId,
	remotePeerId,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.metaLabelIid,
	OLD.aliasIid,
	OLD.localPeerId,
	OLD.remotePeerId,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)
;
CREATE TRIGGER labelchild_log_insert AFTER INSERT ON labelchild
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO labelchild_log
(
	iid,
	agentId,
	parentIid,
	childIid,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.parentIid,
	NEW.childIid,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER labelchild_log_update AFTER UPDATE ON labelchild
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO labelchild_log
(
	iid,
	agentId,
	parentIid,
	childIid,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.parentIid,
	NEW.childIid,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER labelchild_log_delete AFTER DELETE ON labelchild
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO labelchild_log
(
	iid,
	agentId,
	parentIid,
	childIid,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.parentIid,
	OLD.childIid,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)
;
CREATE TRIGGER content_log_insert AFTER INSERT ON content
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO content_log
(
	iid,
	agentId,
	aliasIid,
	contentType,
	metaData,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.aliasIid,
	NEW.contentType,
	NEW.metaData,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER content_log_update AFTER UPDATE ON content
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO content_log
(
	iid,
	agentId,
	aliasIid,
	contentType,
	metaData,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.aliasIid,
	NEW.contentType,
	NEW.metaData,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER content_log_delete AFTER DELETE ON content
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO content_log
(
	iid,
	agentId,
	aliasIid,
	contentType,
	metaData,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.aliasIid,
	OLD.contentType,
	OLD.metaData,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)
;
CREATE TRIGGER profile_log_insert AFTER INSERT ON profile
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO profile_log
(
	iid,
	agentId,
	aliasIid,
	name,
	imgSrc,
	sharedId,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.aliasIid,
	NEW.name,
	NEW.imgSrc,
	NEW.sharedId,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER profile_log_update AFTER UPDATE ON profile
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO profile_log
(
	iid,
	agentId,
	aliasIid,
	name,
	imgSrc,
	sharedId,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.aliasIid,
	NEW.name,
	NEW.imgSrc,
	NEW.sharedId,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER profile_log_delete AFTER DELETE ON profile
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO profile_log
(
	iid,
	agentId,
	aliasIid,
	name,
	imgSrc,
	sharedId,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.aliasIid,
	OLD.name,
	OLD.imgSrc,
	OLD.sharedId,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)
;
CREATE TRIGGER labeledContent_log_insert AFTER INSERT ON labeledContent
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO labeledContent_log
(
	iid,
	agentId,
	contentIid,
	labelIid,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.contentIid,
	NEW.labelIid,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER labeledContent_log_update AFTER UPDATE ON labeledContent
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO labeledContent_log
(
	iid,
	agentId,
	contentIid,
	labelIid,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.contentIid,
	NEW.labelIid,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER labeledContent_log_delete AFTER DELETE ON labeledContent
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO labeledContent_log
(
	iid,
	agentId,
	contentIid,
	labelIid,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.contentIid,
	OLD.labelIid,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)
;
CREATE TRIGGER alias_log_insert AFTER INSERT ON alias
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO alias_log
(
	iid,
	agentId,
	rootLabelIid,
	name,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.rootLabelIid,
	NEW.name,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER alias_log_update AFTER UPDATE ON alias
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO alias_log
(
	iid,
	agentId,
	rootLabelIid,
	name,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.rootLabelIid,
	NEW.name,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER alias_log_delete AFTER DELETE ON alias
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO alias_log
(
	iid,
	agentId,
	rootLabelIid,
	name,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.rootLabelIid,
	OLD.name,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)
;
CREATE TRIGGER labelAcl_log_insert AFTER INSERT ON labelAcl
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO labelAcl_log
(
	iid,
	agentId,
	connectionIid,
	labelIid,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.connectionIid,
	NEW.labelIid,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER labelAcl_log_update AFTER UPDATE ON labelAcl
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO labelAcl_log
(
	iid,
	agentId,
	connectionIid,
	labelIid,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.connectionIid,
	NEW.labelIid,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER labelAcl_log_delete AFTER DELETE ON labelAcl
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO labelAcl_log
(
	iid,
	agentId,
	connectionIid,
	labelIid,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.connectionIid,
	OLD.labelIid,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)
;
CREATE TRIGGER agent_log_insert AFTER INSERT ON agent
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO agent_log
(
	iid,
	agentId,
	uberAliasIid,
	name,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.uberAliasIid,
	NEW.name,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER agent_log_update AFTER UPDATE ON agent
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO agent_log
(
	iid,
	agentId,
	uberAliasIid,
	name,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.uberAliasIid,
	NEW.name,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER agent_log_delete AFTER DELETE ON agent
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO agent_log
(
	iid,
	agentId,
	uberAliasIid,
	name,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.uberAliasIid,
	OLD.name,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)
;
CREATE TRIGGER label_log_insert AFTER INSERT ON label
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO label_log
(
	iid,
	agentId,
	name,
	icon,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.name,
	NEW.icon,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER label_log_update AFTER UPDATE ON label
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO label_log
(
	iid,
	agentId,
	name,
	icon,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.name,
	NEW.icon,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER label_log_delete AFTER DELETE ON label
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO label_log
(
	iid,
	agentId,
	name,
	icon,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.name,
	OLD.icon,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)
;
CREATE TRIGGER notification_log_insert AFTER INSERT ON notification
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO notification_log
(
	iid,
	agentId,
	consumed,
	fromConnectionIid,
	kind,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.consumed,
	NEW.fromConnectionIid,
	NEW.kind,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER notification_log_update AFTER UPDATE ON notification
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO notification_log
(
	iid,
	agentId,
	consumed,
	fromConnectionIid,
	kind,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.consumed,
	NEW.fromConnectionIid,
	NEW.kind,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER notification_log_delete AFTER DELETE ON notification
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO notification_log
(
	iid,
	agentId,
	consumed,
	fromConnectionIid,
	kind,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.consumed,
	OLD.fromConnectionIid,
	OLD.kind,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)
;
CREATE TRIGGER introduction_log_insert AFTER INSERT ON introduction
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO introduction_log
(
	iid,
	agentId,
	aConnectionIid,
	aState,
	bConnectionIid,
	bState,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.aConnectionIid,
	NEW.aState,
	NEW.bConnectionIid,
	NEW.bState,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
)
;
CREATE TRIGGER introduction_log_update AFTER UPDATE ON introduction
REFERENCING NEW ROW AS NEW
FOR EACH ROW
INSERT INTO introduction_log
(
	iid,
	agentId,
	aConnectionIid,
	aState,
	bConnectionIid,
	bState,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	NEW.iid,
	NEW.agentId,
	NEW.aConnectionIid,
	NEW.aState,
	NEW.bConnectionIid,
	NEW.bState,
	NEW.data,
	NEW.deleted,
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
)
;
CREATE TRIGGER introduction_log_delete AFTER DELETE ON introduction
REFERENCING OLD ROW AS OLD
FOR EACH ROW
INSERT INTO introduction_log
(
	iid,
	agentId,
	aConnectionIid,
	aState,
	bConnectionIid,
	bState,
	data,
	deleted,
	created,
	modified,
	createdByAliasIid,
	modifiedByAliasIid,
	auditAction
) VALUES (
	OLD.iid,
	OLD.agentId,
	OLD.aConnectionIid,
	OLD.aState,
	OLD.bConnectionIid,
	OLD.bState,
	OLD.data,
	OLD.deleted,
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
)