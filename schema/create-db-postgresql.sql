
create table agent (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	uberAliasIid varchar(32) not NULL,
	name varchar(50) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
create table alias (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	rootLabelIid varchar(32) not NULL,
	name varchar(50) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
create table connection (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	metaLabelIid varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	localPeerId varchar(32) not NULL,
	remotePeerId varchar(32) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
create table content (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	contentType varchar(32) not NULL,
	metaData text not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
create table introduction (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aConnectionIid varchar(32) not NULL,
	aState varchar(50) not NULL,
	bConnectionIid varchar(32) not NULL,
	bState varchar(50) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
create table label (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	name varchar(50) not NULL,
	icon varchar(50),
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
create table labelAcl (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	connectionIid varchar(32) not NULL,
	labelIid varchar(32) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
create table labelchild (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	parentIid varchar(32) not NULL,
	childIid varchar(32) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
CREATE UNIQUE INDEX labelchild_autogen_parent_child ON labelchild (parentIid,childIid)
;
create table labeledContent (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	contentIid varchar(32) not NULL,
	labelIid varchar(32) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
CREATE UNIQUE INDEX labeledContent_autogen_contentLabel ON labeledContent (contentIid,labelIid)
;
create table login (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	authenticationId varchar(100) not NULL,
	passwordHash char(60) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
create table notification (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	consumed boolean not NULL,
	fromConnectionIid varchar(32) not NULL,
	kind varchar(50) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
create table profile (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	name varchar(50) not NULL,
	imgSrc text not NULL,
	sharedId varchar(32) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)
;
create table agent_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	uberAliasIid varchar(32) not NULL,
	name varchar(50) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
create table alias_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	rootLabelIid varchar(32) not NULL,
	name varchar(50) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
create table connection_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	metaLabelIid varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	localPeerId varchar(32) not NULL,
	remotePeerId varchar(32) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
create table content_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	contentType varchar(32) not NULL,
	metaData text not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
create table introduction_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aConnectionIid varchar(32) not NULL,
	aState varchar(50) not NULL,
	bConnectionIid varchar(32) not NULL,
	bState varchar(50) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
create table labelAcl_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	connectionIid varchar(32) not NULL,
	labelIid varchar(32) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
create table label_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	name varchar(50) not NULL,
	icon varchar(50),
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
create table labelchild_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	parentIid varchar(32) not NULL,
	childIid varchar(32) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
create table labeledContent_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	contentIid varchar(32) not NULL,
	labelIid varchar(32) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
create table login_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	authenticationId varchar(100) not NULL,
	passwordHash char(60) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
create table notification_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	consumed boolean not NULL,
	fromConnectionIid varchar(32) not NULL,
	kind varchar(50) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
create table profile_log (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	name varchar(50) not NULL,
	imgSrc text not NULL,
	sharedId varchar(32) not NULL,
	data text not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	auditId bigserial not NULL,
	auditAction char(6) not NULL,
	primary key(auditId)
)
;
CREATE OR REPLACE FUNCTION login_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO login_log
(
	iid,
	agentId,
	aliasIid,
	authenticationId,
	passwordHash,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION login_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO login_log
(
	iid,
	agentId,
	aliasIid,
	authenticationId,
	passwordHash,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION login_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO login_log
(
	iid,
	agentId,
	aliasIid,
	authenticationId,
	passwordHash,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER login_log_insert AFTER INSERT ON login
FOR EACH ROW
EXECUTE PROCEDURE login_log_insert()
;
CREATE TRIGGER login_log_update AFTER UPDATE ON login
FOR EACH ROW
EXECUTE PROCEDURE login_log_update()
;
CREATE TRIGGER login_log_delete AFTER DELETE ON login
FOR EACH ROW
EXECUTE PROCEDURE login_log_delete()
;
CREATE OR REPLACE FUNCTION connection_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO connection_log
(
	iid,
	agentId,
	metaLabelIid,
	aliasIid,
	localPeerId,
	remotePeerId,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION connection_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO connection_log
(
	iid,
	agentId,
	metaLabelIid,
	aliasIid,
	localPeerId,
	remotePeerId,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION connection_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO connection_log
(
	iid,
	agentId,
	metaLabelIid,
	aliasIid,
	localPeerId,
	remotePeerId,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER connection_log_insert AFTER INSERT ON connection
FOR EACH ROW
EXECUTE PROCEDURE connection_log_insert()
;
CREATE TRIGGER connection_log_update AFTER UPDATE ON connection
FOR EACH ROW
EXECUTE PROCEDURE connection_log_update()
;
CREATE TRIGGER connection_log_delete AFTER DELETE ON connection
FOR EACH ROW
EXECUTE PROCEDURE connection_log_delete()
;
CREATE OR REPLACE FUNCTION labelchild_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO labelchild_log
(
	iid,
	agentId,
	parentIid,
	childIid,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION labelchild_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO labelchild_log
(
	iid,
	agentId,
	parentIid,
	childIid,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION labelchild_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO labelchild_log
(
	iid,
	agentId,
	parentIid,
	childIid,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER labelchild_log_insert AFTER INSERT ON labelchild
FOR EACH ROW
EXECUTE PROCEDURE labelchild_log_insert()
;
CREATE TRIGGER labelchild_log_update AFTER UPDATE ON labelchild
FOR EACH ROW
EXECUTE PROCEDURE labelchild_log_update()
;
CREATE TRIGGER labelchild_log_delete AFTER DELETE ON labelchild
FOR EACH ROW
EXECUTE PROCEDURE labelchild_log_delete()
;
CREATE OR REPLACE FUNCTION content_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO content_log
(
	iid,
	agentId,
	aliasIid,
	contentType,
	metaData,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION content_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO content_log
(
	iid,
	agentId,
	aliasIid,
	contentType,
	metaData,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION content_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO content_log
(
	iid,
	agentId,
	aliasIid,
	contentType,
	metaData,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER content_log_insert AFTER INSERT ON content
FOR EACH ROW
EXECUTE PROCEDURE content_log_insert()
;
CREATE TRIGGER content_log_update AFTER UPDATE ON content
FOR EACH ROW
EXECUTE PROCEDURE content_log_update()
;
CREATE TRIGGER content_log_delete AFTER DELETE ON content
FOR EACH ROW
EXECUTE PROCEDURE content_log_delete()
;
CREATE OR REPLACE FUNCTION profile_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO profile_log
(
	iid,
	agentId,
	aliasIid,
	name,
	imgSrc,
	sharedId,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION profile_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO profile_log
(
	iid,
	agentId,
	aliasIid,
	name,
	imgSrc,
	sharedId,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION profile_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO profile_log
(
	iid,
	agentId,
	aliasIid,
	name,
	imgSrc,
	sharedId,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER profile_log_insert AFTER INSERT ON profile
FOR EACH ROW
EXECUTE PROCEDURE profile_log_insert()
;
CREATE TRIGGER profile_log_update AFTER UPDATE ON profile
FOR EACH ROW
EXECUTE PROCEDURE profile_log_update()
;
CREATE TRIGGER profile_log_delete AFTER DELETE ON profile
FOR EACH ROW
EXECUTE PROCEDURE profile_log_delete()
;
CREATE OR REPLACE FUNCTION labeledContent_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO labeledContent_log
(
	iid,
	agentId,
	contentIid,
	labelIid,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION labeledContent_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO labeledContent_log
(
	iid,
	agentId,
	contentIid,
	labelIid,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION labeledContent_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO labeledContent_log
(
	iid,
	agentId,
	contentIid,
	labelIid,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER labeledContent_log_insert AFTER INSERT ON labeledContent
FOR EACH ROW
EXECUTE PROCEDURE labeledContent_log_insert()
;
CREATE TRIGGER labeledContent_log_update AFTER UPDATE ON labeledContent
FOR EACH ROW
EXECUTE PROCEDURE labeledContent_log_update()
;
CREATE TRIGGER labeledContent_log_delete AFTER DELETE ON labeledContent
FOR EACH ROW
EXECUTE PROCEDURE labeledContent_log_delete()
;
CREATE OR REPLACE FUNCTION alias_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO alias_log
(
	iid,
	agentId,
	rootLabelIid,
	name,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION alias_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO alias_log
(
	iid,
	agentId,
	rootLabelIid,
	name,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION alias_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO alias_log
(
	iid,
	agentId,
	rootLabelIid,
	name,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER alias_log_insert AFTER INSERT ON alias
FOR EACH ROW
EXECUTE PROCEDURE alias_log_insert()
;
CREATE TRIGGER alias_log_update AFTER UPDATE ON alias
FOR EACH ROW
EXECUTE PROCEDURE alias_log_update()
;
CREATE TRIGGER alias_log_delete AFTER DELETE ON alias
FOR EACH ROW
EXECUTE PROCEDURE alias_log_delete()
;
CREATE OR REPLACE FUNCTION labelAcl_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO labelAcl_log
(
	iid,
	agentId,
	connectionIid,
	labelIid,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION labelAcl_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO labelAcl_log
(
	iid,
	agentId,
	connectionIid,
	labelIid,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION labelAcl_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO labelAcl_log
(
	iid,
	agentId,
	connectionIid,
	labelIid,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER labelAcl_log_insert AFTER INSERT ON labelAcl
FOR EACH ROW
EXECUTE PROCEDURE labelAcl_log_insert()
;
CREATE TRIGGER labelAcl_log_update AFTER UPDATE ON labelAcl
FOR EACH ROW
EXECUTE PROCEDURE labelAcl_log_update()
;
CREATE TRIGGER labelAcl_log_delete AFTER DELETE ON labelAcl
FOR EACH ROW
EXECUTE PROCEDURE labelAcl_log_delete()
;
CREATE OR REPLACE FUNCTION agent_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO agent_log
(
	iid,
	agentId,
	uberAliasIid,
	name,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION agent_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO agent_log
(
	iid,
	agentId,
	uberAliasIid,
	name,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION agent_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO agent_log
(
	iid,
	agentId,
	uberAliasIid,
	name,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER agent_log_insert AFTER INSERT ON agent
FOR EACH ROW
EXECUTE PROCEDURE agent_log_insert()
;
CREATE TRIGGER agent_log_update AFTER UPDATE ON agent
FOR EACH ROW
EXECUTE PROCEDURE agent_log_update()
;
CREATE TRIGGER agent_log_delete AFTER DELETE ON agent
FOR EACH ROW
EXECUTE PROCEDURE agent_log_delete()
;
CREATE OR REPLACE FUNCTION label_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO label_log
(
	iid,
	agentId,
	name,
	icon,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION label_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO label_log
(
	iid,
	agentId,
	name,
	icon,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION label_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO label_log
(
	iid,
	agentId,
	name,
	icon,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER label_log_insert AFTER INSERT ON label
FOR EACH ROW
EXECUTE PROCEDURE label_log_insert()
;
CREATE TRIGGER label_log_update AFTER UPDATE ON label
FOR EACH ROW
EXECUTE PROCEDURE label_log_update()
;
CREATE TRIGGER label_log_delete AFTER DELETE ON label
FOR EACH ROW
EXECUTE PROCEDURE label_log_delete()
;
CREATE OR REPLACE FUNCTION notification_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO notification_log
(
	iid,
	agentId,
	consumed,
	fromConnectionIid,
	kind,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION notification_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO notification_log
(
	iid,
	agentId,
	consumed,
	fromConnectionIid,
	kind,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION notification_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO notification_log
(
	iid,
	agentId,
	consumed,
	fromConnectionIid,
	kind,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER notification_log_insert AFTER INSERT ON notification
FOR EACH ROW
EXECUTE PROCEDURE notification_log_insert()
;
CREATE TRIGGER notification_log_update AFTER UPDATE ON notification
FOR EACH ROW
EXECUTE PROCEDURE notification_log_update()
;
CREATE TRIGGER notification_log_delete AFTER DELETE ON notification
FOR EACH ROW
EXECUTE PROCEDURE notification_log_delete()
;
CREATE OR REPLACE FUNCTION introduction_log_insert() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO introduction_log
(
	iid,
	agentId,
	aConnectionIid,
	aState,
	bConnectionIid,
	bState,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'INSERT'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION introduction_log_update() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO introduction_log
(
	iid,
	agentId,
	aConnectionIid,
	aState,
	bConnectionIid,
	bState,
	data,
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
	NEW.created,
	NEW.modified,
	NEW.createdByAliasIid,
	NEW.modifiedByAliasIid,
	'UPDATE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE OR REPLACE FUNCTION introduction_log_delete() RETURNS TRIGGER AS $$
BEGIN
INSERT INTO introduction_log
(
	iid,
	agentId,
	aConnectionIid,
	aState,
	bConnectionIid,
	bState,
	data,
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
	OLD.created,
	OLD.modified,
	OLD.createdByAliasIid,
	OLD.modifiedByAliasIid,
	'DELETE'
);
RETURN NULL;
END;
$$ LANGUAGE plpgsql
;
CREATE TRIGGER introduction_log_insert AFTER INSERT ON introduction
FOR EACH ROW
EXECUTE PROCEDURE introduction_log_insert()
;
CREATE TRIGGER introduction_log_update AFTER UPDATE ON introduction
FOR EACH ROW
EXECUTE PROCEDURE introduction_log_update()
;
CREATE TRIGGER introduction_log_delete AFTER DELETE ON introduction
FOR EACH ROW
EXECUTE PROCEDURE introduction_log_delete()