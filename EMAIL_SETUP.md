# SendGrid Email Configuration Guide

## Setup Instructions for Email Notifications via SendGrid

The Document Management System can send email notifications to students when their documents are approved or rejected using **SendGrid API**. This approach requires no local JAR files and is production-ready.

### Step 1: Create a SendGrid Account

1. Go to [SendGrid.com](https://sendgrid.com) and sign up for a **free account**
   - Free tier includes **100 emails/day**
2. Verify your email address
3. Complete account setup

### Step 2: Get Your SendGrid API Key

1. Log in to your SendGrid dashboard
2. Go to **Settings** → **API Keys**
3. Click **Create API Key**
4. Give it a name: `DocumentManagementSystem`
5. Select **Restricted Access** and enable:
   - ✅ Mail Send
6. Copy the **32-character API key**

### Step 3: Verify Sender Email (Free Tier Only)

For free accounts, you must verify the sender email:

1. Go to **Settings** → **Sender Authentication**
2. Click **Create New Sender**
3. Enter your email details:
   - **From Email:** `noreply@yourdomain.com` or any verified email
   - **From Name:** `College Document Management`
4. Click **Create**
5. Check your inbox for verification email and click the link

### Step 4: Update EmailService Configuration

Edit `src/com/college/docs/EmailService.java`:

```java
private static final String SENDGRID_API_KEY = "SG.your-32-char-api-key-here";
private static final String SENDER_EMAIL = "noreply@yourdomain.com"; // Your verified sender email
private static final String SENDER_NAME = "College Document Management";
```

**Example:**
```java
private static final String SENDGRID_API_KEY = "SG.a1b2c3d4e5f6g7h8i9j0k1l2m3n4o5p6";
private static final String SENDER_EMAIL = "system@college.edu";
private static final String SENDER_NAME = "College Documents";
```

### Step 5: Compile the Project

```cmd
javac -d bin -sourcepath src src\com\college\docs\*.java
```

**✅ No need to download JAR files!** SendGrid uses only standard Java libraries.

### Step 6: Run the Application

```cmd
java -cp bin com.college.docs.LoginGUI
```

### Step 7: Test Email Notifications

1. Log in as **Admin**
2. Select one or more documents
3. Click **Approve** or **Reject**
4. Check the console for: `✅ Email sent successfully to [email]`
5. Open the student's email inbox to verify

### Step 8: Monitor SendGrid Activity

1. Go to **Activity** → **Email Log** in SendGrid dashboard
2. See all sent emails, delivery status, opens, clicks
3. **Bounce/Spam monitoring** - automatic
4. **Detailed analytics** included

---

## Email Template Features

Students will receive professional HTML emails with:
- ✅ **Emoji status indicators** (✅ for APPROVED, ❌ for REJECTED)
- 📄 **Document name** clearly displayed
- 🎨 **Color-coded status badge** (green for approved, red for rejected)
- 🔗 **Call-to-action** buttons with next steps
- 📱 **Mobile-responsive design**
- 🛡️ **Professional header and footer**

---

## Comparison: Gmail vs SendGrid

| Feature | Gmail SMTP | SendGrid API |
|---------|-----------|-------------|
| **Requires JAR files** | ✅ Yes (javax.mail) | ❌ No (Standard Java) |
| **Setup complexity** | Medium (App Password) | Easy (API Key) |
| **Rate limit** | ~100 emails/day | 100 emails/day (free) |
| **Reliability** | Good | Excellent (99.9% uptime) |
| **Analytics** | None | Detailed reporting |
| **Cost** | Free (Gmail account) | Free up to 100/day |
| **Scale** | Limited | Production-ready |
| **Support** | Gmail support | 24/7 SendGrid support |

---

## Troubleshooting

| Issue | Solution |
|-------|----------|
| "Email not configured" message | Check API key is set (not "your-sendgrid-api-key") |
| 401 Unauthorized | API key is incorrect or expired; regenerate in SendGrid dashboard |
| 403 Forbidden | Insufficient permissions on API key; recreate with Mail Send access |
| Emails in spam | Add sender email to contacts; SendGrid has high deliverability |
| 550 error | Sender email not verified; verify in SendGrid settings |
| Free tier limit exceeded | Upgrade to paid plan or wait for next day's quota reset |

---

## Upgrade Options

### Paid Plans (When You Need More)
- **Pro Plan:** 30,000 emails/month ($19.95)
- **Advanced Plans:** Up to 2M emails/month
- Pay-as-you-go: $0.10 per 1000 emails (no monthly fee)

### Alternative Services
- **Mailgun:** 1000 free emails/month
- **AWS SES:** $0.10 per 1000 emails
- **Twilio SendGrid:** Best for large scale

---

## Disabling Email Notifications

To disable emails temporarily, comment out the email sending line in `AdminDashboard.java`:

```java
// new Thread(() -> EmailService.sendDocumentStatusNotification(email, docName, newStatus)).start();
```

---

## Security Best Practices

⚠️ **Important:**
- Never hardcode API keys in production (use environment variables for real deployment)
- Rotate API keys periodically
- For deployment, store keys in:
  - Environment variables: `SENDGRID_API_KEY`
  - Config files (not in Git)
  - Secure vaults (Azure Key Vault, AWS Secrets Manager)

Example for environment variable approach:
```java
private static final String SENDGRID_API_KEY = System.getenv("SENDGRID_API_KEY");
```

---

## Next Steps

1. ✅ Create SendGrid account
2. ✅ Get API key
3. ✅ Verify sender email
4. ✅ Update `EmailService.java`
5. ✅ Compile and run
6. ✅ Test with a document approval
7. ✅ Monitor in SendGrid dashboard

Your email notifications are now production-ready! 🚀

