# Stress test
Done 2025-05-12 with 

## Test setup:
I created 100 facilities with 1000 sub groups each using simulated UI calls (see scripts)

## Results:
- no exceptions or errors on server side
- creation speed didn't slow down
- behaviour in UI:
 - group list seems normal as admin, but slow when logged in as facility manager. it seems that the role view-users is much faster than query-users/query-groups, so a facility manager with view-users is also fast
 - only exception: permissions tab fast as long as you use searching, but slows down when page number >10, the UI does a lot of subcalls
 - policies tab is also a bit slow. the visualization of the details is somehow useless because the html representation is not optimized for high number of groups

## Some thoughts
- Right now, we have one permission per group. If we manage to modify groups (which will probably be possible in a near future release of KeyCloak, the number of permissions will go down to the number of facilities. Then, the UI will behave much better. The server never had a problem with the high nimber of policies (100*1000 = 100000!)