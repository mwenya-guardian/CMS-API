package com.cms.service;

import com.cms.model.Bulletin;
import com.cms.model.Cover;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * ModelToHtmlService: prepares a Thymeleaf context adapted to your Bulletin model,
 * formats LocalDate/LocalTime.
 */
@Service
@AllArgsConstructor
public class ModelToHtmlService {

    private final TemplateEngine templateEngine;

    // Formatters - adjust locale/patterns as needed
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy");
//    private final DateTimeFormatter shortDateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("h:mm a");


    /**
     * Generate a printable PDF byte[] for a single Bulletin.
     * Converts Sets -> Lists and formats dates/times for the template.
     */
    public String generateBulletinHtml(Bulletin bulletin) {
        Context ctx = new Context();

        // Title / content
        ctx.setVariable("title", bulletin.getTitle());
        ctx.setVariable("content", bulletin.getContent() == null ? "" : bulletin.getContent());

        // Cover
        Cover cover = bulletin.getCover() == null ? new Cover() : bulletin.getCover();
        ctx.setVariable("cover", cover);

        // Bulletin date formatted
        String bulletinDateFormatted = bulletin.getBulletinDate() != null
                ? bulletin.getBulletinDate().format(dateFormatter)
                : (bulletin.getScheduledPublishAt() != null ? bulletin.getScheduledPublishAt().toString() : "");
        ctx.setVariable("bulletinDate", bulletinDateFormatted);

        // Schedules: convert Set<Schedule> -> List<Map<String,Object>> and format times/dates
        List<Map<String,Object>> schedulesList = Optional.ofNullable(bulletin.getSchedules())
                .map(Set::stream)
                .orElseGet(Stream::empty)
                .map(s -> {
                    Map<String,Object> m = new HashMap<>();
                    m.put("title", s.getTitle());
                    // scheduledActivities is HashMap<String,String>
                    m.put("scheduledActivities", s.getScheduledActivities() == null ? Collections.emptyMap() : s.getScheduledActivities());
                    m.put("startTime", s.getStartTime() == null ? "" : s.getStartTime().format(timeFormatter));
                    m.put("endTime", s.getEndTime() == null ? "" : s.getEndTime().format(timeFormatter));
                    m.put("scheduledDate", s.getScheduledDate() == null ? "" : s.getScheduledDate().format(dateFormatter));
                    // roleAssignment or activityDetails may not exist in your model — skip if absent
                    // include raw objects if present (for the template to consume)
                    return m;
                })
                .collect(Collectors.toList());

        ctx.setVariable("schedules", schedulesList);

        // Announcements: Set -> List
        List<Object> announcements = Optional.ofNullable(bulletin.getAnnouncements())
                .map(Set::stream)
                .orElseGet(Stream::empty)
                .map(a -> {
                    Map<String,Object> am = new HashMap<>();
                    am.put("title", a.getTitle());
                    am.put("content", a.getContent());
                    return am;
                })
                .collect(Collectors.toList());
        ctx.setVariable("announcements", announcements);

        // OnDuty list: Set -> List (if your OnDuty is not a Set, adapt)
        List<Object> onDutyList = Optional.ofNullable(bulletin.getOnDutyList())
                .map(Set::stream)
                .orElseGet(Stream::empty)
                .map(d -> {
                    Map<String,Object> dm = new HashMap<>();
                    dm.put("role", d.getRole());
                    dm.put("activity", d.getActivity());
                    dm.put("participates", d.getParticipates() != null ? d.getParticipates() : Collections.emptyList());
                    dm.put("date", d.getDate() == null ? "" : d.getDate().format(dateFormatter));
                    return dm;
                })
                .collect(Collectors.toList());
        ctx.setVariable("onDutyList", onDutyList);

        ctx.setVariable("generatedAt", java.time.ZonedDateTime.now().format(DateTimeFormatter.ISO_OFFSET_DATE_TIME));
        // Process Thymeleaf template to HTML
        String html = templateEngine.process("bulletin-pdf", ctx);

        return sanitizeHtml(html);
        // Convert HTML to PDF bytes using OpenHTMLToPDF
//        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
//            PdfRendererBuilder builder = new PdfRendererBuilder();
//            builder.withHtmlContent(html, null); // if images use local paths, pass baseUri as second arg
//            builder.toStream(os);
//            builder.run();
//            return os.toByteArray();
//        } catch (Exception ex) {
//            throw new RuntimeException(ex.getMessage());
//        }

    }


    private String sanitizeHtml(String html) {
        if (html == null) return "";
        // Remove BOM if present
        html = html.replace("\uFEFF", "");
        // Trim leading whitespace and any characters before the first '<'
        int firstLt = html.indexOf('<');
        if (firstLt > 0) {
            html = html.substring(firstLt);
        }
        // Trim trailing/leading whitespace
        return html.trim();
    }
}
