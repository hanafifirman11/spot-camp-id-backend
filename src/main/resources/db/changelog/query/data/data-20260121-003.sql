INSERT INTO themes (code, name, description, is_active, tokens_json, created_at, updated_at)
SELECT 'default', 'Default (Classic)', 'Current default theme', true,
'{"--primary":"#2E5C48","--secondary":"#5C8D74","--background":"#F7F9F6","--primary-green":"#2E5C48","--secondary-green":"#5C8D74","--accent-sand":"#E6B984","--accent-rust":"#D47E56","--text-dark":"#2C3333","--text-light":"#F0F2EE","--bg-nature":"#F7F9F6","--white":"#FFFFFF","--shadow-soft":"0 4px 20px rgba(46, 92, 72, 0.08)","--shadow-card":"0 8px 30px rgba(0, 0, 0, 0.05)","--radius-lg":"16px","--radius-md":"12px","--radius-sm":"8px"}',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM themes WHERE code = 'default');

INSERT INTO themes (code, name, description, is_active, tokens_json, created_at, updated_at)
SELECT 'forest-green', 'Forest Green', 'Deep forest green with earthy accents', false,
'{"--primary":"#1F4B3A","--secondary":"#4F7A5F","--background":"#F3F7F2","--primary-green":"#1F4B3A","--secondary-green":"#4F7A5F","--accent-sand":"#D9B277","--accent-rust":"#C16A4A","--text-dark":"#24312C","--text-light":"#F2F5F0","--bg-nature":"#F3F7F2","--white":"#FFFFFF","--shadow-soft":"0 6px 24px rgba(31, 75, 58, 0.12)","--shadow-card":"0 10px 34px rgba(22, 40, 32, 0.08)","--radius-lg":"16px","--radius-md":"12px","--radius-sm":"8px"}',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM themes WHERE code = 'forest-green');

INSERT INTO themes (code, name, description, is_active, tokens_json, created_at, updated_at)
SELECT 'ocean-blue', 'Ocean Blue', 'Cool ocean blues with crisp surfaces', false,
'{"--primary":"#1F4E79","--secondary":"#4C79A6","--background":"#F4F7FA","--primary-green":"#1F4E79","--secondary-green":"#4C79A6","--accent-sand":"#C9A36A","--accent-rust":"#D76A5A","--text-dark":"#1E2A36","--text-light":"#F2F6FA","--bg-nature":"#F4F7FA","--white":"#FFFFFF","--shadow-soft":"0 6px 22px rgba(31, 78, 121, 0.12)","--shadow-card":"0 10px 32px rgba(18, 38, 66, 0.08)","--radius-lg":"16px","--radius-md":"12px","--radius-sm":"8px"}',
CURRENT_TIMESTAMP, CURRENT_TIMESTAMP
WHERE NOT EXISTS (SELECT 1 FROM themes WHERE code = 'ocean-blue');
