
create cached table alias (
	iid varchar(32) not NULL,
	root_label_iid varchar(32) not NULL,
	name varchar(50) not NULL,
	primary key(iid)
)
;
create cached table connection (
	iid varchar(32) not NULL,
	url varchar(1024) not NULL,
	primary key(iid)
)
;
create cached table content (
	iid varchar(32) not NULL,
	content_type varchar(32) not NULL,
	blob Clob not NULL,
	primary key(iid)
)
;
create cached table label (
	iid varchar(32) not NULL,
	name varchar(50) not NULL,
	primary key(iid)
)
;
create cached table label_acl (
	iid varchar(32) not NULL,
	connection_iid varchar(32) not NULL,
	label_iid varchar(32) not NULL,
	primary key(iid)
)
;
create cached table label_children (
	iid varchar(32) not NULL,
	parent_iid varchar(32) not NULL,
	child_iid varchar(32) not NULL,
	primary key(iid)
)
;
CREATE UNIQUE INDEX label_children_autogen_parent_child ON label_children (parent_iid,child_iid)
;
create cached table labeled_content (
	iid varchar(32) not NULL,
	content_iid varchar(32) not NULL,
	label_iid varchar(32) not NULL,
	primary key(iid)
)
;
CREATE UNIQUE INDEX labeled_content_autogen_content_label ON labeled_content (content_iid,label_iid)