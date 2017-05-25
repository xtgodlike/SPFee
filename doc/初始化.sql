/*==============================================================*/
/* Table: T_BLOB_CONTENT                                        */
/*==============================================================*/
create table T_BLOB_CONTENT
(
   FILE_ID              varchar(32) not null,
   FILE_CONTENT         blob,
   FILENAME             varchar(512),
   primary key (FILE_ID)
);

/*==============================================================*/
/* Table: T_CHANNEL                                             */
/*==============================================================*/
create table T_CHANNEL
(
   CHANNEL_ID           varchar(32) not null,
   FULL_NAME            varchar(512),
   ABBR_NAME            varchar(32),
   CONTRACT_ID          varchar(32) not null,
   OP_STATUS            int,
   CONTACTOR            varchar(32),
   TEL                  varchar(32),
   EMAIL                varchar(32),
   QQ                   varchar(32),
   API_KEY              varchar(32),
   API_PWD              varchar(512),
   primary key (CHANNEL_ID)
);

/*==============================================================*/
/* Table: T_CHANNEL_PIPLE                                       */
/*==============================================================*/
create table T_CHANNEL_PIPLE
(
   CHANNEL_ID           varchar(32) not null,
   PIPLE_ID             varchar(32) not null,
   NOTIFY_URL           varchar(2000),
   primary key (CHANNEL_ID, PIPLE_ID)
);

/*==============================================================*/
/* Table: T_CHANNEL_PRODUCT                                     */
/*==============================================================*/
create table T_CHANNEL_PRODUCT
(
   CHANNEL_ID           varchar(32) not null,
   PRODUCT_ID           varchar(32) not null,
   OP_STATUS            int,
   primary key (CHANNEL_ID, PRODUCT_ID)
);

/*==============================================================*/
/* Table: T_CONTRACT                                            */
/*==============================================================*/
create table T_CONTRACT
(
   CONTRACT_ID          varchar(32) not null,
   PARTY_A              varchar(512) not null,
   PARTY_B              varchar(512) not null,
   PARTY_C              varchar(512),
   VALIDATE_START       date,
   VALIDATE_END         date,
   SIGN_DATE            date,
   CREATE_TIME          datetime,
   CONTRACT_TYPE        int,
   OP_STATUS            int,
   FILE_ID              varchar(32),
   primary key (CONTRACT_ID)
);

/*==============================================================*/
/* Table: T_GLOBAL                                              */
/*==============================================================*/
create table T_GLOBAL
(
   GLOBAL_ID            int not null,
   GLOBAL_NAME          varchar(1000),
   GLOBAL_VALUE         varchar(1000),
   COMMET_DESC          varchar(1000),
   primary key (GLOBAL_ID)
);

/*==============================================================*/
/* Table: T_HOST                                                */
/*==============================================================*/
create table T_HOST
(
   HOST_ID              int not null,
   HOST_NAME            varchar(2000),
   primary key (HOST_ID)
);

/*==============================================================*/
/* Table: T_IMSI_MOBILE                                         */
/*==============================================================*/
create table T_IMSI_MOBILE
(
   IMSI                 varchar(128) not null,
   MOBILE               varchar(10),
   IMPORTTIME           datetime,
   APIKEY               varchar(32),
   primary key (IMSI)
);

/*==============================================================*/
/* Index: I_IMSI_MOBILE_MOBILE                                  */
/*==============================================================*/
create index I_IMSI_MOBILE_MOBILE on T_IMSI_MOBILE
(
   MOBILE
);

/*==============================================================*/
/* Index: I_IMSI_MOBILE_IMPORTTIME                              */
/*==============================================================*/
create index I_IMSI_MOBILE_IMPORTTIME on T_IMSI_MOBILE
(
   IMPORTTIME
);

/*==============================================================*/
/* Index: I_IMSI_MOBILE_APIKEY                                  */
/*==============================================================*/
create index I_IMSI_MOBILE_APIKEY on T_IMSI_MOBILE
(
   APIKEY
);

/*==============================================================*/
/* Table: T_LOCATION                                            */
/*==============================================================*/
create table T_LOCATION
(
   HOST_ID              int not null,
   SEGMENT              varchar(7) not null,
   PROVINCE_ID          int,
   primary key (HOST_ID, SEGMENT)
);

/*==============================================================*/
/* Table: T_MODULE                                              */
/*==============================================================*/
create table T_MODULE
(
   MODULE_ID            varchar(16) not null,
   MODULE_CODE          varchar(64),
   PARENT_M_ID          varchar(16),
   MODULE_NAME          varchar(100),
   MODULE_LINK          varchar(128),
   MODULE_DESC          varchar(512),
   MODULE_LEVEL         int,
   SHOW_SEQ             int,
   MODULE_TYPE          int,
   CREATE_TIME          datetime,
   CREATOR              varchar(16),
   ENABLE_TIME          datetime,
   STATUS               int,
   primary key (MODULE_ID)
);

alter table T_MODULE comment '存储所有模块信息，比如功能模块菜单，按钮等，涉及到权限控制的';

/*==============================================================*/
/* Table: T_ORDER                                               */
/*==============================================================*/
create table T_ORDER
(
   ORDER_ID             varchar(32) not null,
   PIPLE_ID             varchar(32) not null,
   MOBILE               varchar(20),
   CHANNEL_ID           varchar(32) not null,
   PRODUCT_ID           varchar(32) not null,
   T_O_PIPLE_ID         varchar(32),
   ORDER_STATUS         int,
   SUB_STATUS           int,
   PIPLE_ORDER_ID       varchar(128),
   CREATE_TIME          datetime,
   MOD_TIME             datetime,
   COMPLETE_TIME        datetime,
   DEC_STATUS           int,
   RND                  int,
   IMSI                 varchar(128),
   ICCID                varchar(128),
   primary key (ORDER_ID)
);

/*==============================================================*/
/* Table: T_ORDER_EXT                                           */
/*==============================================================*/
create table T_ORDER_EXT
(
   ORDER_ID             varchar(32) not null,
   EXT_KEY              varchar(32) not null,
   EXT_VALUE            varchar(512),
   primary key (ORDER_ID, EXT_KEY)
);

/*==============================================================*/
/* Table: T_ORDER_SUBSTATUS                                     */
/*==============================================================*/
create table T_ORDER_SUBSTATUS
(
   PIPLE_ID             varchar(32) not null,
   SUB_STATUS           int not null,
   SUB_STATUS_DESC      varchar(32),
   primary key (PIPLE_ID, SUB_STATUS)
);

/*==============================================================*/
/* Table: T_PIPLE                                               */
/*==============================================================*/
create table T_PIPLE
(
   PIPLE_ID             varchar(32) not null,
   PIPLE_NAME           varchar(32),
   SUPPLIER_ID          varchar(32) not null,
   OP_STATUS            int,
   CONTRACT_ID          varchar(32),
   PIPLE_URL_A          varchar(512),
   PIPLE_URL_B          varchar(512),
   NOTIFY_URL_A         varchar(512),
   NOTIFY_URL_B         varchar(512),
   CHANNEL_URL_A        varchar(512),
   CHANNEL_URL_B        varchar(512),
   PIPLE_DOC            varchar(32),
   CHANNEL_DOC          varchar(32),
   PIPLE_AUTH_A         varchar(128),
   PIPLE_AUTH_B         varchar(128),
   PIPLE_AUTH_C         varchar(128),
   primary key (PIPLE_ID)
);

/*==============================================================*/
/* Table: T_PIPLE_HOST                                          */
/*==============================================================*/
create table T_PIPLE_HOST
(
   PIPLE_ID             varchar(32) not null,
   HOST_ID              int not null,
   primary key (PIPLE_ID, HOST_ID)
);

/*==============================================================*/
/* Table: T_PIPLE_PRODUCT                                       */
/*==============================================================*/
create table T_PIPLE_PRODUCT
(
   PIPLE_ID             varchar(32) not null,
   PRODUCT_ID           varchar(32) not null,
   PIPLE_PRODUCT_CODE   varchar(32),
   PIPLE_PRODUCT_ABBR_CODE varchar(32),
   OP_STATUS            int,
   primary key (PIPLE_ID, PRODUCT_ID)
);

/*==============================================================*/
/* Table: T_PIPLE_PROVINCE                                      */
/*==============================================================*/
create table T_PIPLE_PROVINCE
(
   PIPLE_ID             varchar(32) not null,
   PROVINCE_ID          int not null,
   PRIORITY             int,
   primary key (PIPLE_ID, PROVINCE_ID)
);

/*==============================================================*/
/* Table: T_PRODUCT                                             */
/*==============================================================*/
create table T_PRODUCT
(
   PRODUCT_ID           varchar(32) not null,
   PRODUCT_NAME         varchar(512),
   PRICE                int,
   primary key (PRODUCT_ID)
);

/*==============================================================*/
/* Table: T_PROVINCE                                            */
/*==============================================================*/
create table T_PROVINCE
(
   PROVINCE_ID          int not null,
   PROVINCE_NAME        varchar(32),
   PROVINCE_ABS_NAME    varchar(32),
   ISO_CODE             varchar(32),
   primary key (PROVINCE_ID)
);

/*==============================================================*/
/* Table: T_ROLE                                                */
/*==============================================================*/
create table T_ROLE
(
   ROLE_ID              varchar(32) not null,
   ROLE_NAME            varchar(100),
   ROLE_TYPE            int,
   ROLE_DESC            varchar(512),
   CREATE_TIME          datetime,
   CREATOR              varchar(16),
   LAST_UPDATE_TIME     datetime,
   LAST_UPDATE_USER     varchar(16),
   STATUS               int,
   WRITABLE             int,
   primary key (ROLE_ID)
);

alter table T_ROLE comment '存储所有角色信息';

/*==============================================================*/
/* Table: T_ROLE_MODULE                                         */
/*==============================================================*/
create table T_ROLE_MODULE
(
   ROLE_ID              varchar(16) not null,
   MODULE_ID            varchar(16) not null,
   primary key (MODULE_ID, ROLE_ID)
);

alter table T_ROLE_MODULE comment '存储角色和模块之间的关系，多对多';

/*==============================================================*/
/* Table: T_SUPPLIER                                            */
/*==============================================================*/
create table T_SUPPLIER
(
   SUPPLIER_ID          varchar(32) not null,
   FULL_NAME            varchar(512),
   ABBR_NAME            varchar(32),
   CONTACTOR            varchar(32),
   TEL                  varchar(32),
   EMAIL                varchar(32),
   QQ                   varchar(32),
   primary key (SUPPLIER_ID)
);

/*==============================================================*/
/* Table: T_USER                                                */
/*==============================================================*/
create table T_USER
(
   USER_ID              varchar(64) not null,
   USER_TYPE            int,
   CP_ID                varchar(32),
   CHANNEL_ID           varchar(32),
   USER_NAME            varchar(32),
   PASSWORD             varchar(32),
   CHN_NAME             varchar(32),
   USER_STATUS          int,
   CREATE_TIME          datetime,
   LEARDER_ID           varchar(32),
   CER_NO               varchar(32),
   BIRTHDAY             datetime,
   LEVEL                int,
   VIRTUAL              int,
   EMAIL                varchar(256),
   TELEPHONE            varchar(32),
   NEWPASSWORD          varchar(32),
   BASE_ID              int not null,
   primary key (USER_ID)
);

alter table T_USER comment '用户信息表';

/*==============================================================*/
/* Table: T_USER_ROLE                                           */
/*==============================================================*/
create table T_USER_ROLE
(
   USER_ID              varchar(64) not null,
   ROLE_ID              varchar(64) not null,
   T_U_USER_ID          varchar(32),
   primary key (ROLE_ID, USER_ID)
);

alter table T_USER_ROLE comment '存储用户和角色之间的关系，多对多';

