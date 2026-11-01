#!/bin/bash
cd -- "$(dirname -- "$0")" || exit 1
python3 scripts/run_local.py "$@"
run_status=$?
if [ "$run_status" -ne 0 ] && [ -t 0 ]; then
    read -r -p 'Press Enter to close this window...' _
fi
exit "$run_status"
