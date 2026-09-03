# Smart Donation — Postman Flow Guide

## Section 1: Auth (7 requests)

### Flow
```
1.1 Register → 1.2 Verify Email → 1.3 Login → Done
```

### How to register any role

**1.1 Register** — change body:
```json
{
  "firstName": "Priya",
  "lastName": "Sharma",
  "email": "priya@test.com",
  "phone": "9876543210",
  "password": "Donor@123",
  "confirmPassword": "Donor@123",
  "role": "DONOR"
}
```

**1.2 Verify Email** — change body:
```json
{
  "email": "priya@test.com",
  "otp": "000000"
}
```

**1.3 Login** — change body:
```json
{
  "email": "priya@test.com",
  "password": "Donor@123"
}
```

### Role examples

| Role | email | password | role |
|------|-------|----------|------|
| DONOR | priya@test.com | Donor@123 | DONOR |
| ADMIN | admin@gmail.com | Admin@123 | ADMIN |
| NGO | helpinghands@gmail.com | Ngo@123 | NGO |
| VOLUNTEER | amit.volunteer@gmail.com | Vol@123 | VOLUNTEER |

### Token auto-save

Login (1.3) auto-detects role and saves:
```
DONOR    → donor_token
NGO      → ngo_token
VOLUNTEER → volunteer_token
ADMIN    → admin_token
```

### Admin one-account rule

First ADMIN registration → 201
Second ADMIN registration → 409 "Admin account already exists"

---

## Sections 2-11: Profile & Admin endpoints

After login, tokens auto-save. Use them in subsequent sections:

| Section | Token | Requests |
|---------|-------|----------|
| 2. Donor Profile | donor_token | Create, Get, Update, Delete |
| 3. NGO Profile | ngo_token | Create, Get, Update, Delete |
| 4. Volunteer Profile | volunteer_token | Create, Get, Update, Delete |
| 5. Volunteer Licence | volunteer_token | Submit, Get |
| 6. Admin → Users | admin_token | List, Get, Role, Count, Deactivate |
| 7. Admin → NGO Verify | admin_token | List, Pending, Get, Verify, Reject |
| 8. Admin → Donor | admin_token | List, Get |
| 9. Admin → Volunteer | admin_token | List, Pending, Get, Verify, Reject |
| 10. Donor → NGOs | donor_token | List, Get |
| 11. Security Tests | all | 401, 403, 400 scenarios |

---

## Quick test (5 minutes)

1. Import collection into Postman
2. **1.1 Register** — body role: DONOR → Send → 201
3. **1.2 Verify Email** — enter OTP → Send → 200
4. **1.3 Login** — Send → 200 → donor_token saved ✅
5. **2.1 Create Donor Profile** → Send → 201
6. Repeat steps 2-4 for ADMIN role
7. **6.1 Get All Users** → Send → 200 ✅
