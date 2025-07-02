package com.cms.service;

import com.cms.model.Event;
import com.cms.model.Publication;
import com.cms.model.Quote;
import com.itextpdf.kernel.pdf.PdfDocument;
import com.itextpdf.kernel.pdf.PdfWriter;
import com.itextpdf.layout.Document;
import com.itextpdf.layout.element.Paragraph;
import com.itextpdf.layout.element.Text;
import com.itextpdf.layout.properties.TextAlignment;
import org.apache.poi.xslf.usermodel.XMLSlideShow;
import org.apache.poi.xslf.usermodel.XSLFSlide;
import org.apache.poi.xslf.usermodel.XSLFTextBox;
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
}