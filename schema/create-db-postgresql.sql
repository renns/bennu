
create table agent (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	uberAliasIid varchar(32) not NULL,
	name varchar(50) not NULL,
	data text not NULL,
	deleted boolean not NULL,
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
	deleted boolean not NULL,
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
	deleted boolean not NULL,
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
	deleted boolean not NULL,
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
	deleted boolean not NULL,
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
	deleted boolean not NULL,
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
	deleted boolean not NULL,
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
	deleted boolean not NULL,
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
	deleted boolean not NULL,
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
	deleted boolean not NULL,
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
	deleted boolean not NULL,
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
	deleted boolean not NULL,
	created timestamp not NULL,
	modified timestamp not NULL,
	createdByAliasIid varchar(32) not NULL,
	modifiedByAliasIid varchar(32) not NULL,
	primary key(iid)
)