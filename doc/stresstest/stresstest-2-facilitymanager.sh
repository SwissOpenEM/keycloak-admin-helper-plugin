#!/bin/bash

#set -e

auth="Bearer eyJhbGciOiJSUzI1NiIsInR5cCIgOiAiSldUIiwia2lkIiA6ICJSVTRja2pXZlRDT0dLN2hsUVlzNlNGNkR3UnFIWU0ydllNUWtoTnItNUJnIn0.eyJleHAiOjE3NDcwNDA5NzQsImlhdCI6MTc0NzA0MDkxNCwianRpIjoib25sdHJ0OjQxZTAzOTY0LTgzMGMtNDQ0ZS05YjEzLWE3NDg4YmRjYzE4YSIsImlzcyI6Imh0dHA6Ly9sb2NhbGhvc3Q6ODAyNC9yZWFsbXMvbWFzdGVyIiwidHlwIjoiQmVhcmVyIiwiYXpwIjoic2VjdXJpdHktYWRtaW4tY29uc29sZSIsInNpZCI6ImMyZWRmYzIwLTczY2MtNGVmMS04MzYxLWE3MmNmNzQyZDczZCIsInNjb3BlIjoib3BlbmlkIGVtYWlsIHByb2ZpbGUifQ.E4cI9iTGqsvYtMJv9iGpTBRn9p7Rl-tyGksUnZyrxgjeLfmGQgH1ueRtaDUn0g4qUMPJHgPVnkdogyYE1wnQFh-nQaqWDjvV-_PIjafec7X3pzYQjnFKsM8vxpKeOo27WBE-jJ6mxA2qYWM9uk3YQBtm_q1WVO_2lOH9MEDxdREw7TVI4DZqpfrBPSATZi6_ShQ72_aAWOMj89AaVx4h3WlOAoiqeY5rmW6UN9Qpoi1YXaZxHhidrC18ujXIlK_TwAMkv0IDID2QmAn8TkTNqNSmwC_fPGgeCt9tyd-6SzpbMqS1VnLb96vip_qHX6OGLDxgM_h9OLaK891GDxqqKA"

# /tmp/groups.txt  is the result of http://localhost:8024/admin/realms/stresstest/groups?max=101
jq -r '.[].id' </tmp/groups.txt >/tmp/grouplist.lst
exec 3</tmp/grouplist.lst
active=0
while read -u 3 uuid; do
  #uuid=e8290d98-3ed2-4f3c-a893-090a18b7d675
  echo "> $uuid"
  for j in $(seq 1000); do
    #echo $uuid $j
    if [ "$uuid $j" = "def2b931-19f6-44b8-b58c-638ea368b53e 487" ]; then
      active=1
    fi
    [ $active -eq 1 ] || continue
    curl -s -f 'http://localhost:8024/admin/realms/stresstest/groups/'$uuid'/children' \
      -X POST \
      -H 'User-Agent: Mozilla/5.0 (X11; Linux x86_64; rv:138.0) Gecko/20100101 Firefox/138.0' \
      -H 'Accept: application/json, text/plain, */*' \
      -H 'Accept-Language: en-US,en;q=0.5' \
      -H 'Accept-Encoding: gzip, deflate, br, zstd' \
      -H "authorization: $auth" \
      -H 'content-type: application/json' \
      -H 'Origin: http://localhost:8024' \
      -H 'Connection: keep-alive' \
      -H 'Sec-Fetch-Dest: empty' \
      -H 'Sec-Fetch-Mode: cors' \
      -H 'Sec-Fetch-Site: same-origin' \
      -H 'Priority: u=4' \
      --data-raw '{"name":"tsg'$j'"}' >/tmp/curlresult.txt
    r=$?
#    [ $r -ne 409 ] || break
    if [ $r -ne 0 ]; then
      echo "> $uuid $j"
      echo "Error $r on $uuid $i"
      echo -n "Enter new auth token: "
      read auth
    fi
  done
done