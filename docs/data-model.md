# Data Model (ER Draft)

**users**(id, name, email, role, dept, status)  
**doors**(id, name, location)  
**events**(id, user_id, door_id, ts, result, reason)  
**shifts**(id, name, on_time, off_time, grace_min)  
**rosters**(user_id, date, shift_id)  
**attendance_daily**(user_id, date, first_in, last_out, late_min, early_min, status)  
**audit_logs**(id, operator_id, action, target, ts, diff)

Relationships:
 users 1..* events; doors 1..* events
 users 1..* rosters; shifts 1..* rosters
 events → attendance_daily (aggregation job)
