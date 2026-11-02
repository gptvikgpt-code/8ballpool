# Free server feasibility check

Goal: assess a zero-cost, remote Android test environment before deploying Pool Trainer RU.

## Candidate
Oracle Cloud Always Free Ampere A1 (ARM64). Eligibility, region capacity, and terms must be verified at provisioning time. **Do not assume Android Emulator or 8 Ball Pool works on this VM.** Nested KVM availability and graphics compatibility are unverified.

## Acceptance tests
1. Provision only resources marked Always Free. Configure cost alerts; do not upgrade to paid resources.
2. Check host architecture: `uname -m`; memory: `free -h`; virtualization: `ls -l /dev/kvm`; `egrep -c '(vmx|svm)' /proc/cpuinfo` (x86 only).
3. Confirm a compatible Android ARM64 system image and emulator on the host, with functional graphics and adequate performance.
4. Boot an emulator and confirm `adb devices`, screenshots, and input control.
5. Confirm 8 Ball Pool is supported and runs in this environment; do not assume Play Store access or game compatibility.
6. Run offline screenshot-based ball detection tests and log accuracy before any autonomous control.
7. Stop if KVM is unavailable, the game cannot run, or free-tier quotas are exceeded. Do not bypass game anti-cheat or Android permission controls.

## References
- https://docs.oracle.com/en-us/iaas/Content/FreeTier/freetier_topic-Always_Free_Resources.htm
- https://developer.android.com/studio/run/emulator-acceleration
- https://github.com/google/android-emulator-container-scripts

Status: plan only; no cloud account or server has been provisioned.
