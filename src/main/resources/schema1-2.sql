CREATE PROCEDURE UpdateSchema1_2
  BEGIN
    IF (SELECT MAX(version) = 1
        FROM mg_schema_version
        WHERE plugin = 'MinigamesBase')
    THEN
      # Change mg_user_score
      ALTER TABLE mg_user_score
        ADD COLUMN uuid BINARY(16) NOT NULL;
      UPDATE mg_user_score
      SET uuid = UNHEX(REPLACE(player_uuid, '-', ''));
      ALTER TABLE mg_user_score
        DROP INDEX unique_index;
      ALTER TABLE mg_user_score
        DROP COLUMN player_uuid;
      ALTER TABLE mg_user_score
        ADD UNIQUE INDEX unique_index (uuid, game);
      ALTER TABLE mg_user_score
        MODIFY COLUMN game VARCHAR(50) NOT NULL;
      ALTER TABLE mg_user_score
        MODIFY COLUMN version DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

      # Change mg_user_ints
      ALTER TABLE mg_user_ints
        ADD COLUMN uuid BINARY(16) NOT NULL;
      UPDATE mg_user_ints
      SET uuid = UNHEX(REPLACE(player_uuid, '-', ''));
      ALTER TABLE mg_user_ints
        DROP INDEX unique_index;
      ALTER TABLE mg_user_ints
        DROP COLUMN player_uuid;
      ALTER TABLE mg_user_ints
        ADD UNIQUE INDEX unique_index (uuid, property);
      ALTER TABLE mg_user_ints
        MODIFY COLUMN property VARCHAR(100) NOT NULL;
      ALTER TABLE mg_user_ints
        MODIFY COLUMN version DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

      # Change mg_user_strings
      ALTER TABLE mg_user_strings
        ADD COLUMN uuid BINARY(16) NOT NULL;
      UPDATE mg_user_strings
      SET uuid = UNHEX(REPLACE(player_uuid, '-', ''));
      ALTER TABLE mg_user_strings
        DROP INDEX unique_index;
      ALTER TABLE mg_user_strings
        DROP COLUMN player_uuid;
      ALTER TABLE mg_user_strings
        ADD UNIQUE INDEX unique_index (uuid, property);
      ALTER TABLE mg_user_strings
        MODIFY COLUMN property VARCHAR(100) NOT NULL;
      ALTER TABLE mg_user_strings
        MODIFY COLUMN version DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

      # Change mg_user_bools
      ALTER TABLE mg_user_bools
        ADD COLUMN uuid BINARY(16) NOT NULL;
      UPDATE mg_user_bools
      SET uuid = UNHEX(REPLACE(player_uuid, '-', ''));
      ALTER TABLE mg_user_bools
        DROP INDEX unique_index;
      ALTER TABLE mg_user_bools
        DROP COLUMN player_uuid;
      ALTER TABLE mg_user_bools
        ADD UNIQUE INDEX unique_index (uuid, property);
      ALTER TABLE mg_user_bools
        MODIFY COLUMN property VARCHAR(100) NOT NULL;
      ALTER TABLE mg_user_bools
        MODIFY COLUMN version DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

      # Change mg_user_doubles
      ALTER TABLE mg_user_doubles
        ADD COLUMN uuid BINARY(16) NOT NULL;
      UPDATE mg_user_doubles
      SET uuid = UNHEX(REPLACE(player_uuid, '-', ''));
      ALTER TABLE mg_user_doubles
        DROP INDEX unique_index;
      ALTER TABLE mg_user_doubles
        DROP COLUMN player_uuid;
      ALTER TABLE mg_user_doubles
        ADD UNIQUE INDEX unique_index (uuid, property);
      ALTER TABLE mg_user_doubles
        MODIFY COLUMN property VARCHAR(100) NOT NULL;
      ALTER TABLE mg_user_doubles
        MODIFY COLUMN version DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP;

      # Create UUID to name cache
      CREATE TABLE IF NOT EXISTS mg_name_cache (
        uuid      BINARY(16)   NOT NULL,
        name      VARCHAR(255) NOT NULL,
        timestamp DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
        PRIMARY KEY (uuid)
      );

      INSERT INTO mg_name_cache (uuid, name, timestamp)
        SELECT
          m1.uuid,
          m1.player_name,
          m1.version
        FROM mg_user_score AS m1
          LEFT JOIN mg_user_score AS m2
            ON m1.uuid = m2.uuid AND m1.version > m2.version
        WHERE m2.uuid IS NULL;

      ALTER TABLE mg_user_score
        DROP COLUMN player_name;

      INSERT INTO mg_schema_version (version, plugin) VALUES (2, 'MinigamesBase');
    END IF;
  END;

CALL UpdateSchema1_2();