DO $$
    DECLARE
        r RECORD;
    BEGIN
        -- 모든 테이블을 루프를 통해 삭제
        FOR r IN (SELECT tablename FROM pg_tables WHERE schemaname = 'public') LOOP
                EXECUTE 'DROP TABLE IF EXISTS ' || quote_ident(r.tablename) || ' CASCADE';
            END LOOP;
    END $$;