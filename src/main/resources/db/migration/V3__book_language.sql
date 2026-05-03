ALTER TABLE books ADD COLUMN language VARCHAR(64) NOT NULL DEFAULT 'English';

UPDATE books SET language = 'Spanish' WHERE title LIKE '%Harry Potter%';
UPDATE books SET language = 'French' WHERE title = '1984';
UPDATE books SET language = 'Japanese' WHERE title = 'Introduction to Algorithms';
