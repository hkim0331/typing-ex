DROP TABLE IF EXISTS roll_calls;

CREATE TABLE roll_calls (
  id SERIAL PRIMARY KEY,
  login varchar(20),
  pt  int,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP);
