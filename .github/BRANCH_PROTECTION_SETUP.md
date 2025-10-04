# Branch Protection Setup

To require passing unit tests before merging code, you need to configure branch protection rules in GitHub. Here's how:

## Steps to Configure Branch Protection

1. **Go to your repository on GitHub**
2. **Navigate to Settings > Branches**
3. **Click "Add rule" or edit existing rules for your main branches (main, master, develop)**
4. **Configure the following settings:**

### Required Settings

- ✅ **Require a pull request before merging**
  - ✅ Require approvals: 1 (or more as needed)
  - ✅ Dismiss stale PR approvals when new commits are pushed
  - ✅ Require review from code owners (if you have a CODEOWNERS file)

- ✅ **Require status checks to pass before merging**
  - ✅ Require branches to be up to date before merging
  - ✅ Search for status checks: `test` (this will find our CI job)

- ✅ **Require conversation resolution before merging**

- ✅ **Restrict pushes that create files larger than 100 MB**

### Optional Settings

- ✅ **Require linear history** (prevents merge commits)
- ✅ **Include administrators** (applies rules to admins too)
- ✅ **Allow force pushes** (disable this for better protection)
- ✅ **Allow deletions** (disable this for better protection)

## What This Achieves

- **Prevents direct pushes** to protected branches
- **Requires pull requests** for all changes
- **Blocks merging** if tests fail
- **Ensures code quality** through required reviews
- **Maintains clean history** with linear commits

## Testing the Setup

1. Create a new branch
2. Make a change that breaks a test
3. Create a pull request
4. Verify that the PR cannot be merged due to failing tests
5. Fix the test and push again
6. Verify that the PR can now be merged

## Current CI Configuration

The workflow file (`.github/workflows/ci.yml`) is configured to:
- Run on pushes and pull requests to main, master, and develop branches
- Test against Java 8, 11, and 17
- Cache Maven dependencies for faster builds
- Upload test results as artifacts for debugging
