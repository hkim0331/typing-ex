# initd.bd

後から migrations は増えるんで、それを考えてナンバリングすべき。
90 番代まで使ってしまったのは良くなかった。

## CAUSION DEPRECATED
typing_ex=# \d
```
                 List of relations
 Schema |        Name        |   Type   |  Owner
--------+--------------------+----------+----------
 public | drills             | table    | postgres
 public | drills_id_seq      | sequence | postgres
 public | ragtime_migrations | table    | postgres
 public | results            | table    | postgres
 public | results_id_seq     | sequence | postgres
 public | roll_calls         | table    | postgres
 public | roll_calls_id_seq  | sequence | postgres
 public | schema_migrations  | table    | postgres
 public | stat               | table    | postgres
(9 rows)
```

### drills
```
typing_ex=# \d drills
                                        Table "public.drills"
  Column   |            Type             | Collation | Nullable |              Default
-----------+-----------------------------+-----------+----------+------------------------------------
 id        | integer                     |           | not null | nextval('drills_id_seq'::regclass)
 text      | text                        |           |          |
 timestamp | timestamp without time zone |           |          | CURRENT_TIMESTAMP
Indexes:
    "drills_pkey" PRIMARY KEY, btree (id)
```

### results
```
typing_ex=# \d results
                                        Table "public.results"
  Column   |            Type             | Collation | Nullable |               Default
-----------+-----------------------------+-----------+----------+-------------------------------------
 id        | integer                     |           | not null | nextval('results_id_seq'::regclass)
 login     | character varying(32)       |           |          |
 pt        | integer                     |           |          | 0
 timestamp | timestamp without time zone |           |          | CURRENT_TIMESTAMP
Indexes:
    "results_pkey" PRIMARY KEY, btree (id)
```

### stat
```
typing_ex=# \d stat
                                 Table "public.stat"
   Column   |            Type             | Collation | Nullable |      Default
------------+-----------------------------+-----------+----------+-------------------
 stat       | character varying(20)       |           |          |
 updated_at | timestamp without time zone |           |          | CURRENT_TIMESTAMP
```

### roll_calls
```
typing_ex=# \d roll_calls
                                        Table "public.roll_calls"
   Column   |            Type             | Collation | Nullable |                Default
------------+-----------------------------+-----------+----------+----------------------------------------
 id         | integer                     |           | not null | nextval('roll_calls_id_seq'::regclass)
 login      | character varying(20)       |           |          |
 pt         | integer                     |           |          |
 created_at | timestamp without time zone |           |          | CURRENT_TIMESTAMP
Indexes:
    "roll_calls_pkey" PRIMARY KEY, btree (id)
```
