DELETE FROM seances WHERE course_id IS NULL;
DELETE FROM courses WHERE formation_id IS NULL;

ALTER TABLE courses ALTER COLUMN formation_id SET NOT NULL;
ALTER TABLE seances ALTER COLUMN course_id SET NOT NULL;

UPDATE users
SET organisation_id = (
    SELECT id FROM organisations WHERE slug = 'techacademy-maroc' LIMIT 1
)
WHERE email = 'admin.org@demo.lms'
  AND EXISTS (SELECT 1 FROM organisations WHERE slug = 'techacademy-maroc')
  AND organisation_id IS DISTINCT FROM (
      SELECT id FROM organisations WHERE slug = 'techacademy-maroc' LIMIT 1
  );
