
INSERT INTO email_template (id, subject, body_html, body_plain, type, from_email_address)
VALUES ('27ca47cb-7799-4a9f-8a65-4f4c8808839f', 'Confirm nostalogic.net registration',
        '<p>Thank you for registering on nostalogic.net! We need to verify this email address to complete your registration.</p> ' ||
        '<p>Click the following link to confirm you registered with this email:</p> ' ||
        '<p><a href="{{base_url}}/link/regconfirm?code={{reg_code}}">Click here</a></p>',
        'Thank you for registering on nostalogic.net! We need to verify this email address to complete your registration. ' ||
        '\nClick the following link to confirm you registered with this email: ' ||
        '\n{{base_url}}/link/regconfirm?code={{reg_code}}',
        'REGISTRATION_CONFIRM', 'noreply@nostalogic.net') ON CONFLICT DO NOTHING;

INSERT INTO email_template (id, subject, body_html, body_plain, type, from_email_address)
VALUES ('f3a95d4e-ca99-40a9-93f0-2e220b5f1613', 'Reset nostalogic.net password',
        '<p>A password reset was requested for the account linked to this email on nostalogic.net.</p> ' ||
        '<p>If you did not request this you can safely ignore this email and no further action is required.</p> ' ||
        '<p>To reset your password use the following link:</p> ' ||
        '<p><a href="{{base_url}}/link/passwordreset?code={{reset_code}}">Click here</a></p>' ||
        '<p>This link will expire in 7 days.</p>',
        'A password reset was requested for the account linked to this email on nostalogic.net. ' ||
        '\nIf you did not request this you can safely ignore this email and no further action is required. ' ||
        '\nTo reset your password use the following link: ' ||
        '\n{{base_url}}/link/passwordreset?code={{reset_code}}' ||
        '\nThis link will expire in 7 days.',
        'PASSWORD_RESET', 'noreply@nostalogic.net') ON CONFLICT DO NOTHING;
