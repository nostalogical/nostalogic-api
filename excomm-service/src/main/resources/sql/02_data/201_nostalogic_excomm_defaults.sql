
INSERT INTO email_template (id, subject, body_html, body_plain, type, from_email_address)
VALUES ('27ca47cb-7799-4a9f-8a65-4f4c8808839f', 'Confirm nostalogic.net registration',
        '<p>Thank you for registering on nostalogic.net! We need to verify this email address to complete your registration.</p> ' ||
        '<p>Click the following link to confirm you registered with this email:</p> ' ||
        '<p><a href="{{base_url}}/regconfirm?code={{reg_code}}">Click here</a></p>',
        'Thank you for registering on nostalogic.net! We need to verify this email address to complete your registration. ' ||
        '\nClick the following link to confirm you registered with this email: ' ||
        '\n{{base_url}}/regconfirm?code={{reg_code}}',
        0, 'noreply@nostalogic.net') ON CONFLICT DO NOTHING;
