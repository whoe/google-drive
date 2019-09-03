-- comment to work properly updateDb on supply
-- begin SYS_FILE
-- alter table SYS_FILE add column GDRIVE_ID varchar(255) ^
-- alter table SYS_FILE add column USE_DEFAULT_FILE_API boolean ^
-- alter table SYS_FILE add column DTYPE varchar(100) ^
-- update SYS_FILE set DTYPE = 'googledrive$ExtFileDescriptor' where DTYPE is null ^
-- end SYS_FILE

