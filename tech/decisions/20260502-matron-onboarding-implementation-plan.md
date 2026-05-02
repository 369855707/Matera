# Matron Onboarding Implementation Plan

This note turns the onboarding spec into a concrete build plan.

## Database Design

### Guiding rule

Store images and identity documents outside the database. Keep only metadata and business state in SQL tables.

### Tables

#### `matron_onboarding_applications`

One application per matron user, with a state machine that controls submission and review.

Suggested columns:

- `id` bigint primary key
- `user_id` bigint not null unique
- `status` varchar not null
- `real_name` varchar not null
- `phone` varchar
- `city` varchar
- `age` int
- `years_of_experience` int
- `service_description` text
- `self_intro` text
- `review_notes` text
- `submitted_at` timestamp
- `reviewed_at` timestamp
- `reviewed_by` bigint
- `created_at` timestamp
- `updated_at` timestamp

#### `matron_onboarding_documents`

One application can have many uploaded files.

Suggested columns:

- `id` bigint primary key
- `application_id` bigint not null
- `document_type` varchar not null
- `object_key` varchar not null
- `file_url` varchar not null
- `mime_type` varchar
- `file_size` bigint
- `sort_order` int
- `created_at` timestamp

### Application states

- `DRAFT`: user has started but not submitted
- `PENDING_REVIEW`: submitted and waiting for admin action
- `APPROVED`: accepted and eligible for public visibility
- `REJECTED`: rejected with review notes

### Existing table changes

- Keep `users.avatar` as a URL string
- Keep `matron_profiles` as the public profile table
- Add a field or rule that only approved applications may create or activate a matron profile

## API Design

### Public or authenticated matron APIs

#### Create application

- `POST /api/matron-applications`
- Creates or updates a draft application for the current matron user

#### Submit application

- `POST /api/matron-applications/{id}/submit`
- Moves a draft to `PENDING_REVIEW`

#### Get current application

- `GET /api/matron-applications/me`
- Returns the current user’s application and documents

#### Upload document

- `POST /api/matron-applications/{id}/documents`
- Accepts multipart upload or a signed-upload callback payload
- Stores the file outside the database and persists metadata only

### Admin APIs

#### List applications

- `GET /api/admin/matron-applications`
- Supports filters by `status` and pagination

#### Get application detail

- `GET /api/admin/matron-applications/{id}`

#### Approve application

- `POST /api/admin/matron-applications/{id}/approve`
- Marks the application approved and activates the matron

#### Reject application

- `POST /api/admin/matron-applications/{id}/reject`
- Requires a rejection reason

### Response shape

Return:

- application id
- user id
- status
- submitted and reviewed timestamps
- uploaded document list
- review notes
- public profile activation state

## Implementation Order

### Phase 1: Data model

1. Add onboarding application entity.
2. Add onboarding document entity.
3. Add repositories and status enums.
4. Migrate the existing public matron lookup to honor approval state.

### Phase 2: Matron endpoints

1. Create draft and submit endpoints.
2. Add current-application read endpoint.
3. Add upload endpoint for profile photos and ID images.
4. Validate file type, size, and ownership.

### Phase 3: Admin review endpoints

1. List pending and reviewed applications.
2. Inspect one application with its documents.
3. Approve or reject with notes.
4. Activate the matron profile only on approval.

### Phase 4: Public visibility and cleanup

1. Hide unapproved applicants from public matron browsing.
2. Add status labels to admin responses.
3. Add tests for the full approval and rejection flow.

## Key Decisions

- Keep the active matron profile separate from the application record.
- Use object storage for all uploaded files.
- Do not expose an applicant in public browsing until approval.
- Treat rejection as a normal state, not an error.

## Open Questions

- Whether a rejected application should be edited in place or cloned into a new submission.
- Whether the ID number itself should be stored or only the document image.
- Whether multipart upload or signed upload should be the default implementation first.

