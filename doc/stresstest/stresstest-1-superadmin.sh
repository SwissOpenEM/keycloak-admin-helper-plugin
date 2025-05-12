#!/bin/sh

set -e

for i in $(seq 100); do
  curl -f 'http://localhost:8024/admin/realms/stresstest/groups' \
    -X POST \
    -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:138.0) Gecko/20100101 Firefox/138.0' \
    -H 'Accept: application/json, text/plain, */*' \
    -H 'Accept-Language: en-US,en;q=0.5' \
    -H 'Accept-Encoding: gzip, deflate, br, zstd' \
    -H 'authorization: Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJSVTRja2pXZlRDT0dLN2hsUVlzNlNGNkR3UnFIWU0ydllNUWtoTnItNUJnIn0.eyJleHAiOjE3NDcwMzkyNTMsImlhdCI6MTc0NzAzOTE5MywianRpIjoib25sdHJ0OmRkOWZlMDI0LTgxYTUtNDkzYi05NWQ0LTU1YmVlYzQ4YThjNyIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODAyNC9yZWFsbXMvbWFzdGVyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic2VjdXJpdHktYWRtaW4tY29uc29sZSIsInNpZCI6ImMyZWRmYzIwLTczY2MtNGVmMS04MzYxLWE3MmNmNzQyZDczZCIsInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUifQ.ltmjzbgNWW9De1EumIUrFH_eeSPcpeytX_Y-c0izW-Ky_E2HMJUJrL7nmkzzLgESC_0_rzFNuuADPVqqL55jhoeSc7o2OubyyHN33Ffyt0pzxRsoAigp9ex8-o2KeC1WXH0Bk8vzESSBXnhXGza61y-k4A4ozz-npHa2jTxm4zbBu15suYF2Z0-SB_Pu4rsd-IP_YaKZY71FZeOsmuihNia8Ie-E-TImlJAhY6qaD7YCazwvnnvDOcM15T_-DPhFQ3xFECowWECeO7bqPAK722GotRSESuZGZa5Sl0IOwid5JkSsWZ710apcZTXxnzf5Jjz4kV40JdHlD35vmu7Lrg' \
    -H 'content-type: application/json' \
    -H 'Origin: http://localhost:8024' \
    -H 'Connection: keep-alive' \
    -H 'Sec-Fetch-Dest: empty' \
    -H 'Sec-Fetch-Mode: cors' \
    -H 'Sec-Fetch-Site: same-origin' \
    -H 'Priority: u=4' \
    --data-raw '{"name":"testgroup'$i'--initnewfacility"}'
done