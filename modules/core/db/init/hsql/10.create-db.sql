-- begin SEC_USER
alter table SEC_USER add column DTYPE varchar(100) ^
update SEC_USER set DTYPE = 'sociallogindemo_SocialUser' where DTYPE is null ^
-- end SEC_USER
