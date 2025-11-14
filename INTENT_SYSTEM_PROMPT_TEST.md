# Intent System Prompt Testing Guide

## Overview
This document provides test cases for validating the system prompt descriptions for each IntentType.
Each test includes the expected intent classification and a sample of the expected AI response (max 1 phrase).

---

## Test Cases

### 1. FIND_JOB Intent

#### High Confidence Tests
| Input | Expected Intent | Confidence | Expected Response Type |
|-------|----------------|------------|----------------------|
| "I need a job" | FIND_JOB | >0.7 | Ask what kind of work they're looking for |
| "Looking for work" | FIND_JOB | >0.7 | Ask about their field or experience |
| "I'm job hunting" | FIND_JOB | >0.7 | Ask about role preferences |
| "Find me employment" | FIND_JOB | >0.7 | Ask about desired work type |

**Expected Response Pattern**: Natural question asking about role type, field, experience level, or location (one at a time)

#### Low Confidence Tests
| Input | Expected Intent | Confidence | Expected Response Type |
|-------|----------------|------------|----------------------|
| "I'm thinking about my career" | FIND_JOB | <0.7 | Ask if they're looking for a new role |
| "Not happy at work" | FIND_JOB | <0.7 | Ask about job change consideration |

**Expected Response Pattern**: Casual confirmation question like "Are you looking for a new role?" or "Thinking about a job change?"

---

### 2. FIND_TALENT Intent

#### High Confidence Tests
| Input | Expected Intent | Confidence | Expected Response Type |
|-------|----------------|------------|----------------------|
| "I need to hire a developer" | FIND_TALENT | >0.7 | Ask what kind of developer they need |
| "Looking for candidates" | FIND_TALENT | >0.7 | Ask about the role they're hiring for |
| "Need to recruit someone" | FIND_TALENT | >0.7 | Ask about candidate requirements |
| "Hiring for my team" | FIND_TALENT | >0.7 | Ask about the position details |

**Expected Response Pattern**: One conversational question about role type or candidate requirements

#### Low Confidence Tests
| Input | Expected Intent | Confidence | Expected Response Type |
|-------|----------------|------------|----------------------|
| "My team needs help" | FIND_TALENT | <0.7 | Ask if they're looking to hire |
| "Thinking about growing the team" | FIND_TALENT | <0.7 | Casual question about hiring plans |

**Expected Response Pattern**: "Are you looking to hire someone?"

---

### 3. CREATE_RESUME Intent

#### High Confidence Tests
| Input | Expected Intent | Confidence | Expected Response Type |
|-------|----------------|------------|----------------------|
| "Help with my resume" | CREATE_RESUME | >0.7 | Ask about their experience/role |
| "Need to update my CV" | CREATE_RESUME | >0.7 | Ask about work history or skills |
| "Can you review my profile?" | CREATE_RESUME | >0.7 | Ask about their background |
| "Resume advice needed" | CREATE_RESUME | >0.7 | Ask about their field/experience |

**Expected Response Pattern**: Question about one aspect: current role, years of experience, key skills, or achievements

#### Low Confidence Tests
| Input | Expected Intent | Confidence | Expected Response Type |
|-------|----------------|------------|----------------------|
| "My profile needs work" | CREATE_RESUME | <0.7 | Ask if working on resume |
| "Career documentation" | CREATE_RESUME | <0.7 | Light supportive question |

**Expected Response Pattern**: "Working on your resume?"

---

### 4. UNRELATED Intent

#### High Confidence Tests
| Input | Expected Intent | Confidence | Expected Response Type |
|-------|----------------|------------|----------------------|
| "Write me a Python function" | UNRELATED | >0.7 | Politely explain scope limitation |
| "What's the weather today?" | UNRELATED | >0.7 | Redirect to job-related topics |
| "Tell me a joke" | UNRELATED | >0.7 | Friendly scope explanation |
| "Help with my homework" | UNRELATED | >0.7 | Explain job/hiring focus |

**Expected Response Pattern**: Brief, friendly explanation that Workley helps with jobs, hiring, and resumes only

#### Low Confidence Tests
| Input | Expected Intent | Confidence | Expected Response Type |
|-------|----------------|------------|----------------------|
| "Hello" | UNRELATED | <0.7 | Gentle redirect to services |
| "What can you do?" | UNRELATED | <0.7 | Brief mention of specialization |

**Expected Response Pattern**: "I specialize in job searching and hiring. Looking for a role, hiring someone, or need resume help?"

---

## Testing Instructions

### Manual Testing via Chat Interface
1. Start the application
2. Send each test input
3. Verify the intent classification matches expected
4. Check that confidence threshold triggers correct prompt (high/low)
5. Validate response is conversational and follows the expected pattern

### Success Criteria
- ✓ Intent correctly classified
- ✓ Confidence level appropriate
- ✓ Response is natural and conversational
- ✓ Response asks ONE thing at a time (not multiple questions)
- ✓ Response tone matches confidence level (confident vs. uncertain)
- ✓ Response stays within scope for UNRELATED intent

### Edge Cases to Test
| Input | Notes |
|-------|-------|
| "job job job job" | Should still classify correctly |
| "..." | Should be UNRELATED |
| Very long message with multiple intents | Check which intent dominates |
| Empty string | Error handling |
| Special characters: !@#$%^&*() | Should be UNRELATED |
| Mixed language | Depends on model capability |

---

## Sample Expected Responses

### FIND_JOB (High Confidence)
**Input**: "I need a job"
**Expected**: "What kind of work are you looking for?"

### FIND_TALENT (High Confidence)
**Input**: "I need to hire a developer"
**Expected**: "What kind of developer are you looking for?"

### CREATE_RESUME (High Confidence)
**Input**: "Help with my resume"
**Expected**: "What's your current role or field?"

### UNRELATED (High Confidence)
**Input**: "Write me a Python function"
**Expected**: "I'm focused on helping with job searching and hiring. I can help you find roles, candidates, or work on your resume. What would you like to explore?"

---

## Notes for Improvement
- Monitor if responses are too verbose or too brief
- Check if confidence threshold (0.7) needs adjustment
- Validate that system prompts produce natural conversation flow
- Ensure responses don't sound robotic or template-like
