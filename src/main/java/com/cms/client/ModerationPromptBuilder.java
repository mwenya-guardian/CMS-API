package com.cms.client;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Moderation categories and a prompt builder that instructs an LLM
 * to classify user comments into categories, provide confidences,
 * flagging decisions and short rationales. Designed to return JSON.
 */
public final class ModerationPromptBuilder {

    private ModerationPromptBuilder() {}

    public enum ModerationCategory {
        HATE_SPEECH("Hate Speech",
                "Language targeting a protected class (race, religion, gender, sexual orientation, nationality, disability) with demeaning, dehumanizing or degrading content.",
                "Flag if direct slurs, calls for exclusion, or demeaning statements are present; severity HIGH when explicit calls to violence or dehumanization appear."),
        HARASSMENT("Harassment",
                "Abusive language directed at an individual (insults, belittling, repeated attacks) that is not necessarily a protected class attack.",
                "Flag when targeted attacks, repeated insults, or attempts to humiliate a specific individual appear; severity MEDIUM/HIGH for persistent or sexualized harassment."),
        BULLYING("Bullying",
                "Aggressive or controlling language intended to intimidate or coerce an individual or group repeatedly.",
                "Flag when the comment shows intent to intimidate, isolate, or repeatedly target someone; severity scales with specificity and repetition."),
        PROFANITY("Profanity / Vulgar Language",
                "Use of swear words or crude language that may be offensive but not necessarily targeted or threatening.",
                "Do not flag for moderation by default unless profanity is targeted at a person/group or coupled with threats—then flag as HARASSMENT or HATE_SPEECH."),
        SPAM("Spam / Advertising",
                "Irrelevant promotional content, repeated links, obvious bot messages or attempts to drive traffic/transactions.",
                "Flag when content is promotional, repeated, or contains malicious/irrelevant links; severity typically LOW but can be raised if links are malicious."),
        MISINFORMATION("Misinformation",
                "Claims that are false or unverified and presented as fact (public-health, elections, fabricated events).",
                "Flag when a comment asserts demonstrably false facts or conspiracy claims; severity MEDIUM/HIGH if dangerous (health/ safety)."),
        VIOLENCE("Violence / Threats",
                "Statements advocating, praising, or threatening physical harm to people or property.",
                "Always flag. HIGH severity for direct threats or calls to harm; MEDIUM if glorifying violence without immediate threat."),
        SELF_HARM("Self-Harm / Suicide",
                "Expressions of intent to self-harm, encourage suicide, or provide instructions for self-harm.",
                "Always flag and escalate per policy (sensitive safety). Provide HIGH severity and recommend immediate human review / safety workflow."),
        ILLEGAL_ACTIVITY("Illegal Activity / Criminal Facilitation",
                "Instructions or admissions for committing crimes (e.g., how to make explosives, hack, steal).",
                "Flag when there are instructions or admission of intent to commit crimes; severity HIGH when stepwise instruction is present."),
        TOXIC_TONE("Toxic Tone / Abusive Tone",
                "Overall negative tone (rudeness, aggression, demeaning sarcasm) that may not map cleanly to targeted harassment.",
                "Flag when conversational tone is aggressively toxic even without specific insults; severity MEDIUM when persistent."),
        PERSONAL_INFO("Personal Info Disclosure / Doxxing",
                "Sharing private personal information about someone (addresses, phone numbers, IDs) without consent.",
                "Always flag and treat as HIGH severity; recommend immediate removal and human review."),
        SEXUAL_CONTENT("Sexual / Explicit Content",
                "Explicit sexual language or pornographic descriptions that may be inappropriate for the platform.",
                "Flag when explicit sexual content is present or when sexual content targets an individual; severity depends on explicitness and target.");

        private final String label;
        private final String description;
        private final String flaggingRule;

        ModerationCategory(String label, String description, String flaggingRule) {
            this.label = label;
            this.description = description;
            this.flaggingRule = flaggingRule;
        }

        public String label() {
            return label;
        }

        public String description() {
            return description;
        }

        public String flaggingRule() {
            return flaggingRule;
        }
    }

    /**
     * Build a prompt for a single comment.
     * Returns a long instruction asking for JSON output with categories,
     * confidences, flagged boolean, severity and short rationale.
     */
    public static String buildPromptForComment() {
        String categoriesBlock = Arrays.stream(ModerationCategory.values())
                .map(c -> String.format("- Moderation Categories: %s", c.label()))
                .collect(Collectors.joining("\n"));

        String categoriesIntro = "Moderation categories \n" + categoriesBlock + "\n\n";

         String sampleOutput = "Example output format (strict JSON):\n"
                 + "[\n" +
                 "  {\n" +
                 "    \"commentId\": \"comment456\", \n" +
                 "    \"sentiment\": \"negative\",\n" +
                 "    \"sentimentScore\": 0.2,\n" +
                 "    \"moderation\": {\n" +
                 "      \"flagged\": true,\n" +
                 "      \"categories\": [\"SELF_HARM\", \"BULLYING\"],\n" +
                 "      \"confidence\": 0.9\n" +
                 "    }\n" +
                 "  }\n" +
                 "]";

        return categoriesIntro + "Return only the JSON object. " + sampleOutput;
    }

    private static String escapeJson(String s) {
        if (s == null) return "";
        return s.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
