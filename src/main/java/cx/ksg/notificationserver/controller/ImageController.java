package cx.ksg.notificationserver.controller;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import cx.ksg.notificationserver.dto.ImageDto;
import cx.ksg.notificationserver.service.ImageService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.constraints.NotBlank;

@RestController
public class ImageController {
    
    @Autowired
    private ImageService imageService;

    @GetMapping(path = "/image/{uuid}")
    public void getImage(@PathVariable @NotBlank String uuid, HttpServletResponse response) throws IOException
    {
        if(StringUtils.isBlank(uuid))
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        ImageDto image = imageService.getImageByUuid(uuid);
        if(image == null)
        {
            response.sendError(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType(image.getContentType());
        response.setContentLengthLong(image.getSize());
        try(InputStream is = FileUtils.openInputStream(new File(imageService.getImagePath(image.getFilename()))); OutputStream os = response.getOutputStream())
        {
            IOUtils.copy(is, os);
        }
    }
}
