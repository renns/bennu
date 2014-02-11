
create cached table agent (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	name varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	primary key(iid)
)
;
create cached table alias (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	rootLabelIid varchar(32) not NULL,
	name varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	primary key(iid)
)
;
create cached table connection (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	localPeerId varchar(32) not NULL,
	remotePeerId varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	primary key(iid)
)
;
create cached table content (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aliasIid varchar(32) not NULL,
	contentType varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	primary key(iid)
)
;
create cached table introduction (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	aConnectionIid varchar(32) not NULL,
	aState varchar(50) not NULL,
	bConnectionIid varchar(32) not NULL,
	bState varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	primary key(iid)
)
;
create cached table label (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	name varchar(50) not NULL,
	icon varchar(50),
	data Clob not NULL,
	deleted bit not NULL,
	primary key(iid)
)
;
create cached table labelAcl (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	connectionIid varchar(32) not NULL,
	labelIid varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	primary key(iid)
)
;
create cached table labelchild (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	parentIid varchar(32) not NULL,
	childIid varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	primary key(iid)
)
;
CREATE UNIQUE INDEX labelchild_autogen_parent_child ON labelchild (parentIid,childIid)
;
create cached table labeledContent (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	contentIid varchar(32) not NULL,
	labelIid varchar(32) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	primary key(iid)
)
;
CREATE UNIQUE INDEX labeledContent_autogen_contentLabel ON labeledContent (contentIid,labelIid)
;
create cached table notification (
	iid varchar(32) not NULL,
	agentId varchar(32) not NULL,
	consumed bit not NULL,
	fromConnectionIid varchar(32) not NULL,
	kind varchar(50) not NULL,
	data Clob not NULL,
	deleted bit not NULL,
	primary key(iid)
)