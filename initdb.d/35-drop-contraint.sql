\c typing;

-- 削除
alter table results drop constraint results_users_nick_fkey;

-- 無効化
-- ALTER TABLE results DISABLE TRIGGER ALL;
