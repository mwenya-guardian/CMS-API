package com.cms.service;

import com.cms.model.Announcement;
import com.cms.model.Bulletin;
import com.cms.model.Event;
import com.cms.model.OnDuty;
import com.cms.model.Publication;
import com.cms.model.Quote;
import com.cms.model.Schedule;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.apache.poi.xwpf.usermodel.XWPFRun;
import org.springframework.stereotype.Service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class ExportService {
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    
    public byte[] exportPublicationsToPdf(List<Publication> publications, String title) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Add title
        if (title != null && !title.isEmpty()) {
            document.add(new Paragraph(title)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
        }
        
        // Add publications
        for (Publication publication : publications) {
            document.add(new Paragraph(publication.getTitle())
                    .setFontSize(16)
                    .setBold()
                    .setMarginTop(15));
            
            if (publication.getAuthor() != null) {
                document.add(new Paragraph("By: " + publication.getAuthor())
                        .setFontSize(12)
                        .setItalic());
            }
            
            document.add(new Paragraph("Date: " + publication.getDate().format(DATE_FORMATTER))
                    .setFontSize(10));
            
            document.add(new Paragraph(publication.getContent())
                    .setFontSize(12)
                    .setMarginTop(10));
            
            if (publication.getTags() != null && !publication.getTags().isEmpty()) {
                document.add(new Paragraph("Tags: " + String.join(", ", publication.getTags()))
                        .setFontSize(10)
                        .setItalic()
                        .setMarginTop(5));
            }
        }
        
        document.close();
        return baos.toByteArray();
    }
    
    public byte[] exportPublicationsToPpt(List<Publication> publications, String title) throws IOException {
        XMLSlideShow ppt = new XMLSlideShow();
        
        // Title slide
        if (title != null && !title.isEmpty()) {
            XSLFSlide titleSlide = ppt.createSlide();
            XSLFTextBox titleBox = titleSlide.createTextBox();
            titleBox.setText(title);
            titleBox.setAnchor(new java.awt.Rectangle(50, 200, 600, 100));
        }
        
        // Content slides
        for (Publication publication : publications) {
            XSLFSlide slide = ppt.createSlide();
            
            // Title
            XSLFTextBox titleBox = slide.createTextBox();
            titleBox.setText(publication.getTitle());
            titleBox.setAnchor(new java.awt.Rectangle(50, 50, 600, 60));
            
            // Content
            XSLFTextBox contentBox = slide.createTextBox();
            String content = publication.getContent();
            if (content.length() > 500) {
                content = content.substring(0, 500) + "...";
            }
            contentBox.setText(content);
            contentBox.setAnchor(new java.awt.Rectangle(50, 120, 600, 300));
            
            // Author and date
            if (publication.getAuthor() != null) {
                XSLFTextBox authorBox = slide.createTextBox();
                authorBox.setText("By: " + publication.getAuthor() + " | " + 
                                publication.getDate().format(DATE_FORMATTER));
                authorBox.setAnchor(new java.awt.Rectangle(50, 450, 600, 30));
            }
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ppt.write(baos);
        ppt.close();
        return baos.toByteArray();
    }
    
    public byte[] exportEventsToPdf(List<Event> events, String title) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Add title
        if (title != null && !title.isEmpty()) {
            document.add(new Paragraph(title)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
        }
        
        // Add events
        for (Event event : events) {
            document.add(new Paragraph(event.getTitle())
                    .setFontSize(16)
                    .setBold()
                    .setMarginTop(15));
            
            document.add(new Paragraph("Category: " + event.getCategory().name())
                    .setFontSize(12));
            
            document.add(new Paragraph("Start: " + event.getStartDate().format(DATE_FORMATTER))
                    .setFontSize(10));
            
            if (event.getEndDate() != null) {
                document.add(new Paragraph("End: " + event.getEndDate().format(DATE_FORMATTER))
                        .setFontSize(10));
            }
            
            if (event.getLocation() != null) {
                document.add(new Paragraph("Location: " + event.getLocation())
                        .setFontSize(10));
            }
            
            document.add(new Paragraph(event.getDescription())
                    .setFontSize(12)
                    .setMarginTop(10));
        }
        
        document.close();
        return baos.toByteArray();
    }
    
    public byte[] exportQuotesToPdf(List<Quote> quotes, String title) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);
        
        // Add title
        if (title != null && !title.isEmpty()) {
            document.add(new Paragraph(title)
                    .setFontSize(20)
                    .setBold()
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
        }
        
        // Add quotes
        for (Quote quote : quotes) {
            document.add(new Paragraph("\"" + quote.getText() + "\"")
                    .setFontSize(14)
                    .setItalic()
                    .setMarginTop(15)
                    .setTextAlignment(TextAlignment.CENTER));
            
            document.add(new Paragraph("— " + quote.getAuthor())
                    .setFontSize(12)
                    .setBold()
                    .setTextAlignment(TextAlignment.RIGHT)
                    .setMarginTop(5));
            
            if (quote.getSource() != null) {
                document.add(new Paragraph("Source: " + quote.getSource())
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT));
            }
            
            if (quote.getCategory() != null) {
                document.add(new Paragraph("Category: " + quote.getCategory())
                        .setFontSize(10)
                        .setTextAlignment(TextAlignment.RIGHT));
            }
        }
        
        document.close();
        return baos.toByteArray();
    }

    public byte[] exportBulletinToPdf(Bulletin bulletin) throws IOException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PdfWriter writer = new PdfWriter(baos);
        PdfDocument pdf = new PdfDocument(writer);
        Document document = new Document(pdf);

        
        // Add title
        document.add(new Paragraph("Church Bulletin")
                .setFontSize(24)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(10));
        
        document.add(new Paragraph(bulletin.getTitle())
                .setFontSize(18)
                .setBold()
                .setTextAlignment(TextAlignment.CENTER)
                .setMarginBottom(20));
        
        // Add date
        if (bulletin.getBulletinDate() != null) {
            document.add(new Paragraph("Date: " + bulletin.getBulletinDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")))
                    .setFontSize(12)
                    .setTextAlignment(TextAlignment.CENTER)
                    .setMarginBottom(20));
        }
        
        // Add cover content
        if (bulletin.getCover() != null) {
            if (bulletin.getCover().getWelcomeMessage() != null) {
                document.add(new Paragraph(bulletin.getCover().getWelcomeMessage())
                        .setFontSize(14)
                        .setMarginBottom(15));
            }
            
            if (bulletin.getCover().getDocumentName() != null) {
                document.add(new Paragraph("Document: " + bulletin.getCover().getDocumentName())
                        .setFontSize(12)
                        .setMarginBottom(10));
            }
            
            if (bulletin.getCover().getFooterMessage() != null) {
                document.add(new Paragraph(bulletin.getCover().getFooterMessage())
                        .setFontSize(10)
                        .setItalic()
                        .setMarginTop(10));
            }
        }
        
        // Add schedules
        if (bulletin.getSchedules() != null && !bulletin.getSchedules().isEmpty()) {
            document.add(new Paragraph("Schedule")
                    .setFontSize(16)
                    .setBold()
                    .setMarginTop(20)
                    .setMarginBottom(10));
            
            for (Schedule schedule : bulletin.getSchedules()) {
                document.add(new Paragraph(schedule.getTitle() + " - " + schedule.getStartTime() + " to " + schedule.getEndTime())
                        .setFontSize(12)
                        .setMarginBottom(5));
                if (schedule.getScheduledActivities() != null) {
                    schedule.getScheduledActivities().forEach((key, value)->{
                        document.add(new Paragraph(key + "\t" + value)
                                .setFontSize(12)
                                .setMarginBottom(2));
                    });
                }
            }
        }
        
        // Add announcements
        if (bulletin.getAnnouncements() != null && !bulletin.getAnnouncements().isEmpty()) {
            document.add(new Paragraph("Announcements")
                    .setFontSize(16)
                    .setBold()
                    .setMarginTop(20)
                    .setMarginBottom(10));
            
            for (Announcement announcement : bulletin.getAnnouncements()) {
                document.add(new Paragraph(announcement.getTitle())
                        .setFontSize(14)
                        .setBold()
                        .setMarginBottom(5));
                
                if (announcement.getContent() != null) {
                    document.add(new Paragraph(announcement.getContent())
                            .setFontSize(12)
                            .setMarginBottom(10));
                }
            }
        }
        
        // Add on duty list
        if (bulletin.getOnDutyList() != null && !bulletin.getOnDutyList().isEmpty()) {
            document.add(new Paragraph("On Duty Today")
                    .setFontSize(16)
                    .setBold()
                    .setMarginTop(20)
                    .setMarginBottom(10));
            
            for (OnDuty duty : bulletin.getOnDutyList()) {
                document.add(new Paragraph(duty.getRole())
                        .setFontSize(14)
                        .setBold()
                        .setMarginBottom(5));
                
                if (duty.getActivity() != null) {
                    document.add(new Paragraph("Activity: " + duty.getActivity())
                            .setFontSize(12)
                            .setMarginBottom(5));
                }
                
                document.add(new Paragraph("Participants: " + String.join(", ", duty.getParticipates()))
                        .setFontSize(12)
                        .setMarginBottom(10));
            }
        }
        
        document.close();
        return baos.toByteArray();
    }
    
    public byte[] exportBulletinToWord(Bulletin bulletin) throws IOException {
        XWPFDocument document = new XWPFDocument();
        
        // Add title
        XWPFParagraph titleParagraph = document.createParagraph();
        titleParagraph.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
        XWPFRun titleRun = titleParagraph.createRun();
        titleRun.setText("Church Bulletin");
        titleRun.setBold(true);
        titleRun.setFontSize(24);
        
        // Add bulletin title
        XWPFParagraph bulletinTitleParagraph = document.createParagraph();
        bulletinTitleParagraph.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
        XWPFRun bulletinTitleRun = bulletinTitleParagraph.createRun();
        bulletinTitleRun.setText(bulletin.getTitle());
        bulletinTitleRun.setBold(true);
        bulletinTitleRun.setFontSize(18);
        
        // Add date
        if (bulletin.getBulletinDate() != null) {
            XWPFParagraph dateParagraph = document.createParagraph();
            dateParagraph.setAlignment(org.apache.poi.xwpf.usermodel.ParagraphAlignment.CENTER);
            XWPFRun dateRun = dateParagraph.createRun();
            dateRun.setText("Date: " + bulletin.getBulletinDate().format(DateTimeFormatter.ofPattern("EEEE, MMMM d, yyyy")));
            dateRun.setFontSize(12);
        }
        
        // Add cover content
        if (bulletin.getCover() != null) {
            if (bulletin.getCover().getWelcomeMessage() != null) {
                XWPFParagraph welcomeParagraph = document.createParagraph();
                XWPFRun welcomeRun = welcomeParagraph.createRun();
                welcomeRun.setText(bulletin.getCover().getWelcomeMessage());
                welcomeRun.setFontSize(14);
            }
            
            if (bulletin.getCover().getDocumentName() != null) {
                XWPFParagraph documentNameParagraph = document.createParagraph();
                XWPFRun documentNameRun = documentNameParagraph.createRun();
                documentNameRun.setText("Document: " + bulletin.getCover().getDocumentName());
                documentNameRun.setFontSize(12);
            }
            
            if (bulletin.getCover().getFooterMessage() != null) {
                XWPFParagraph footerParagraph = document.createParagraph();
                XWPFRun footerRun = footerParagraph.createRun();
                footerRun.setText(bulletin.getCover().getFooterMessage());
                footerRun.setFontSize(10);
                footerRun.setItalic(true);
            }
        }
        
        // Add schedules
        if (bulletin.getSchedules() != null && !bulletin.getSchedules().isEmpty()) {
            XWPFParagraph scheduleTitleParagraph = document.createParagraph();
            XWPFRun scheduleTitleRun = scheduleTitleParagraph.createRun();
            scheduleTitleRun.setText("Schedule");
            scheduleTitleRun.setBold(true);
            scheduleTitleRun.setFontSize(16);
            
            for (Schedule schedule : bulletin.getSchedules()) {
                XWPFParagraph scheduleParagraph = document.createParagraph();
                XWPFRun scheduleRun = scheduleParagraph.createRun();
                scheduleRun.setText(schedule.getTitle() + " - " + schedule.getStartTime() + " to " + schedule.getEndTime());
                scheduleRun.setFontSize(12);
            }
        }
        
        // Add announcements
        if (bulletin.getAnnouncements() != null && !bulletin.getAnnouncements().isEmpty()) {
            XWPFParagraph announcementsTitleParagraph = document.createParagraph();
            XWPFRun announcementsTitleRun = announcementsTitleParagraph.createRun();
            announcementsTitleRun.setText("Announcements");
            announcementsTitleRun.setBold(true);
            announcementsTitleRun.setFontSize(16);
            
            for (Announcement announcement : bulletin.getAnnouncements()) {
                XWPFParagraph announcementTitleParagraph = document.createParagraph();
                XWPFRun announcementTitleRun = announcementTitleParagraph.createRun();
                announcementTitleRun.setText(announcement.getTitle());
                announcementTitleRun.setBold(true);
                announcementTitleRun.setFontSize(14);
                
                if (announcement.getContent() != null) {
                    XWPFParagraph announcementDescParagraph = document.createParagraph();
                    XWPFRun announcementDescRun = announcementDescParagraph.createRun();
                    announcementDescRun.setText(announcement.getContent());
                    announcementDescRun.setFontSize(12);
                }
            }
        }
        
        // Add on duty list
        if (bulletin.getOnDutyList() != null && !bulletin.getOnDutyList().isEmpty()) {
            XWPFParagraph dutyTitleParagraph = document.createParagraph();
            XWPFRun dutyTitleRun = dutyTitleParagraph.createRun();
            dutyTitleRun.setText("On Duty Today");
            dutyTitleRun.setBold(true);
            dutyTitleRun.setFontSize(16);
            
            for (OnDuty duty : bulletin.getOnDutyList()) {
                XWPFParagraph dutyRoleParagraph = document.createParagraph();
                XWPFRun dutyRoleRun = dutyRoleParagraph.createRun();
                dutyRoleRun.setText(duty.getRole());
                dutyRoleRun.setBold(true);
                dutyRoleRun.setFontSize(14);
                
                if (duty.getActivity() != null) {
                    XWPFParagraph dutyActivityParagraph = document.createParagraph();
                    XWPFRun dutyActivityRun = dutyActivityParagraph.createRun();
                    dutyActivityRun.setText("Activity: " + duty.getActivity());
                    dutyActivityRun.setFontSize(12);
                }
                
                XWPFParagraph dutyParticipantsParagraph = document.createParagraph();
                XWPFRun dutyParticipantsRun = dutyParticipantsParagraph.createRun();
                dutyParticipantsRun.setText("Participants: " + String.join(", ", duty.getParticipates()));
                dutyParticipantsRun.setFontSize(12);
            }
        }
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        document.write(baos);
        document.close();
        return baos.toByteArray();
    }
}