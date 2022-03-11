create database typing;
create user ${TYPING_USER} with ${TYPING_PASSWORD};
grant all on database typing to ${TYPING_ISER};
