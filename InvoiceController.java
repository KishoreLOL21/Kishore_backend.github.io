package org.example;

import com.google.zxing.qrcode.QRCodeWriter;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;

import javax.servlet.http.HttpServletResponse;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/api/invoice")
public class InvoiceController {

    @PostMapping("/generate")
    public void generateInvoice(@RequestBody Invoice invoice, HttpServletResponse response) throws IOException, WriterException {
        PDDocument document = new PDDocument();
        PDPage page = new PDPage();
        document.addPage(page);

        PDPageContentStream contentStream = new PDPageContentStream(document, page);

        // Invoice Title
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 14);
        contentStream.beginText();
        contentStream.newLineAtOffset(300, 750);
        contentStream.showText("INVOICE");
        contentStream.endText();

        // Invoice Details
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 720);
        contentStream.showText("Invoice Number: " + invoice.getInvoiceNumber());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Bill From: " + invoice.getBillFrom());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Bill To: " + invoice.getBillTo());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Contact Number: " + invoice.getContactNumber());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("GST Number: " + invoice.getGstNumber());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Customer Name: " + invoice.getCustomerName());
        contentStream.newLineAtOffset(0, -15);
        contentStream.showText("Invoice Date: " + invoice.getInvoiceDate());
        contentStream.endText();

        // QR Code
        String qrCodeURL = "https://example.com/invoice/" + invoice.getInvoiceNumber();
        QRCodeWriter qrCodeWriter = new QRCodeWriter();
        BitMatrix bitMatrix = qrCodeWriter.encode(qrCodeURL, BarcodeFormat.QR_CODE, 200, 200);
        ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
        MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
        byte[] pngData = pngOutputStream.toByteArray();
        PDImageXObject qrCodeImage = PDImageXObject.createFromByteArray(document, pngData, "QRCode");
        contentStream.drawImage(qrCodeImage, 475, 520, 100, 100);

        // Table Header
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(50, 500);
        contentStream.showText("Description");
        contentStream.newLineAtOffset(200, 0);
        contentStream.showText("Quantity");
        contentStream.newLineAtOffset(100, 0);
        contentStream.showText("Unit Price");
        contentStream.newLineAtOffset(100, 0);
        contentStream.showText("Total");
        contentStream.endText();

        // Table Content
        contentStream.setFont(PDType1Font.HELVETICA, 10);
        int yOffset = 480;
        for (Item item : invoice.getItems()) {
            contentStream.beginText();
            contentStream.newLineAtOffset(50, yOffset);
            contentStream.showText(item.getDescription());
            contentStream.newLineAtOffset(200, 0);
            contentStream.showText(String.valueOf(item.getQuantity()));
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText(String.valueOf(item.getUnitPrice()));
            contentStream.newLineAtOffset(100, 0);
            contentStream.showText(String.valueOf(item.getTotal()));
            contentStream.endText();
            yOffset -= 20;
        }

        // Total Amount
        contentStream.setFont(PDType1Font.HELVETICA_BOLD, 10);
        contentStream.beginText();
        contentStream.newLineAtOffset(350, yOffset - 20);
        contentStream.showText("Total: " + invoice.getItems().stream().mapToDouble(Item::getTotal).sum());
        contentStream.endText();

        contentStream.close();

        // Set the response content type and headers
        response.setContentType("application/pdf");
        response.setHeader("Content-Disposition", "attachment; filename=invoice.pdf");

        // Write the PDF to the response output stream
        document.save(response.getOutputStream());
        document.close();
    }
}

// Invoice class to map the JSON request body
class Invoice {
    private String invoiceNumber;
    private String billFrom;
    private String billTo;
    private String contactNumber;
    private String gstNumber;
    private String customerName;
    private String invoiceDate;
    private List<Item> items;

    // getters and setters
    public String getInvoiceNumber() {
        return invoiceNumber;
    }

    public void setInvoiceNumber(String invoiceNumber) {
        this.invoiceNumber = invoiceNumber;
    }

    public String getBillFrom() {
        return billFrom;
    }

    public void setBillFrom(String billFrom) {
        this.billFrom = billFrom;
    }

    public String getBillTo() {
        return billTo;
    }

    public void setBillTo(String billTo) {
        this.billTo = billTo;
    }

    public String getContactNumber() {
        return contactNumber;
    }

    public void setContactNumber(String contactNumber) {
        this.contactNumber = contactNumber;
    }

    public String getGstNumber() {
        return gstNumber;
    }

    public void setGstNumber(String gstNumber) {
        this.gstNumber = gstNumber;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getInvoiceDate() {
        return invoiceDate;
    }

    public void setInvoiceDate(String invoiceDate) {
        this.invoiceDate = invoiceDate;
    }

    public List<Item> getItems() {
        return items;
    }

    public void setItems(List<Item> items) {
        this.items = items;
    }
}

class Item {
    private String description;
    private int quantity;
    private double unitPrice;
    private double total;

    // getters and setters
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public void setUnitPrice(double unitPrice) {
        this.unitPrice = unitPrice;
    }

    public double getTotal() {
        return total;
    }

    public void setTotal(double total) {
        this.total = total;
    }
}
