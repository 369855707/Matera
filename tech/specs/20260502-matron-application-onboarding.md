# Matron Application Onboarding

## Goal

Allow a matron to self-register, submit onboarding information, upload profile photos and identity documents, and then wait for admin review before becoming visible to mothers.

## Current State

The repository already supports:

- Public user registration and login via `/api/auth/register` and `/api/auth/login`
- Matron profile editing via `/api/users/profile/matron`
- Admin creation of users and matron profiles via `/api/admin/users` and `/api/admin/users/matron-profiles`
- Avatar fields on `User` and matron profile DTOs

The repository does not yet support:

- Multipart file upload endpoints
- Identity document storage
- An application queue or review workflow
- Application status tracking such as `PENDING`, `APPROVED`, or `REJECTED`
- Admin review actions for approve/reject with notes
- A distinct “applicant” state separate from a fully active matron

## Non-goals

- Building the frontend admin dashboard in this change
- Building image processing, face recognition, or OCR in the first version
- Replacing the current public matron browsing endpoints
- Changing the existing order or review flows

## Scope

- Pages:
  - Matron onboarding form for self-registration
  - Admin review page or admin review API usage in Swagger for now
- APIs:
  - Create onboarding application
  - Upload photos and ID documents
  - Query application status
  - Admin approve or reject application
- Data:
  - Application record
  - Uploaded file metadata
  - Review status and review notes
- Permissions:
  - Matron can create and view only their own application
  - Admin can list, inspect, approve, and reject applications
- Copy/text:
  - Clear status labels: `DRAFT`, `PENDING_REVIEW`, `APPROVED`, `REJECTED`

## User Flow

1. A matron registers as a normal user or logs in.
2. The user opens the onboarding form.
3. The user fills in profile data and uploads:
   - Profile photos
   - Identity card front and back
   - Optional supporting certifications
4. The system saves the submission as `PENDING_REVIEW`.
5. The matron can see submission status and any rejection reason.
6. Admin reviews the application and approves or rejects it.
7. On approval, the matron becomes active and visible in public browsing.

## Data Changes

### New application entity

Add a new onboarding/application model, separate from the active matron profile, to avoid making incomplete submissions visible too early.

Suggested fields:

- `id`
- `user_id`
- `status`
- `real_name`
- `phone`
- `city`
- `age`
- `years_of_experience`
- `service_description`
- `self_intro`
- `review_notes`
- `submitted_at`
- `reviewed_at`
- `reviewed_by`

### New document entity

Store uploaded file metadata separately from business tables.

Suggested fields:

- `id`
- `application_id`
- `file_type` (`PROFILE_PHOTO`, `ID_FRONT`, `ID_BACK`, `CERTIFICATE`)
- `object_key`
- `file_url`
- `mime_type`
- `file_size`
- `sort_order`
- `created_at`

### Existing model changes

- Add application status to user or matron onboarding state as needed
- Keep avatar and public profile image fields as URL strings
- Do not store image binaries directly in the database

## Acceptance Criteria

- A matron can submit an onboarding application after registration.
- The application can include multiple uploaded images.
- The system stores uploaded files outside the database and saves only metadata in the database.
- The application is not publicly visible until approved.
- Admin can list pending applications and inspect all submitted data and files.
- Admin can approve or reject with a reason.
- The matron can view the current application status.
- Rejected applications can be edited and resubmitted.

## Recommended Technical Approach

- Use object storage for files:
  - `MinIO` for local development
  - `OSS` or `COS` for production
- Use signed upload URLs or a backend multipart upload endpoint.
- Store only file metadata and object keys in the database.
- Keep onboarding applications separate from the active matron profile until approval.
- Change public matron listing to only return approved records.

## Risks

- Upload complexity increases if we support both multipart and direct-to-storage upload.
- Large image uploads need size limits and content-type validation.
- If application and active profile are merged too early, incomplete data could leak into public browsing.
- Rejection/resubmission rules need a clear state machine to avoid inconsistent records.

## Rollback Plan

- Keep the onboarding flow behind new endpoints and new tables.
- If the feature is not acceptable, remove the new endpoints and stop using the new tables.
- Do not migrate current active matron profiles into the new flow until the workflow is stable.

## Verification

- Register or log in as a matron.
- Submit a new onboarding application with at least one profile photo and one ID document.
- Confirm the application lands in pending status.
- Confirm public matron browsing does not show the record yet.
- Approve the application as admin and confirm it becomes visible.
- Reject the application and confirm the rejection reason is returned to the matron.

## Open Questions

- Should ID card number itself be stored, or only the images?
- Should profile photos be required before submission, or optional?
- Should a rejected application be editable in place, or resubmitted as a new version?
- Should admin approval create the active matron profile, or should the active profile be created at submission time and hidden until approval?

