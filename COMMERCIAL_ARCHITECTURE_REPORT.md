# Commercial Architecture Refinement Report

## 1. Overview
RShotsBilling has been upgraded to a professional SaaS architecture centered around the **VENDOR** (RShots Consultancy) as the platform owner. The system now supports multi-tenant isolation, automated onboarding, and secure first-login procedures.

## 2. Key Architecture Changes

### Role Hierarchy
- **LEVEL 1: VENDOR**: Platform owner (rauphinkelvin8@gmail.com). Global visibility and control over all companies, devices, and settings.
- **LEVEL 2: COMPANY ADMIN**: Created by the Vendor. Manages company-specific operations (Staff, Branding, KRA, M-Pesa).
- **LEVEL 3: STAFF**: Created by Company Admin. Specific roles with granular permissions.

### Data Isolation
- Strictly enforced `companyId` scoping.
- **Vendor Access**: Logic updated to allow the Vendor account global access regardless of `companyId`.

## 3. Automated Onboarding Flow

### Step 1: Company Registration (Vendor Dashboard)
- Vendor uses the new **Company Registration Wizard**.
- Automatically generates:
    - **Company ID** (e.g., `comp_172251...`)
    - **Initial Activation Code**
    - **Temporary Password** for the Company Admin.
- Displays summary with Copy/Print options.

### Step 2: First Login & Security Update
- Company Admin logs in with the temporary password.
- System detects `isTemporaryPassword` flag and redirects to **Password Change Screen**.
- Admin sets a new secure password; flag is cleared in Firestore.

### Step 3: Company Setup Wizard
- After password change, Admin is guided through the **Setup Wizard**.
- Configures: Logo, Theme, Receipts, Currency, KRA PIN, eTIMS, and M-Pesa.
- Flags `setupComplete` as true upon finishing.

## 4. Cross-Platform Consistency
- **Shared Logic**: All repository logic and model updates are in the `shared` module.
- **Identical UI**: Registration and Setup Wizards are implemented in `commonMain`, ensuring identical behavior on Android and Desktop.
- **Real-time Sync**: Registered devices and company settings sync instantly across all platforms via centralized Firestore.

## 5. Summary of Modified Files
- `BusinessConfig.kt`: Added `VENDOR` role and session flags.
- `StaffMember.kt` & `Company.kt`: Updated with SaaS/Onboarding fields.
- `CompanyRepository.kt`: Implemented automated registration and setup logic.
- `LoginScreen.kt`: Added redirection logic.
- `CompanyRegistrationWizard.kt`: New 4-step vendor onboarding UI.
- `PasswordChangeScreen.kt`: New security enforcement UI.
- `CompanySetupWizard.kt`: New 4-step admin configuration UI.
- `App.kt`: Integrated new screens into the navigation graph.
