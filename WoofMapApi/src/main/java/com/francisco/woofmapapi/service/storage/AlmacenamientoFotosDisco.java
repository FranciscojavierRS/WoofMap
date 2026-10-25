package com.francisco.woofmapapi.service.storage;

import com.francisco.woofmapapi.exception.InvalidFileException;
import com.francisco.woofmapapi.exception.StorageException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

@Service
public class AlmacenamientoFotosDisco implements AlmacenamientoFotos {

    private static final Set<String> MIME_PERMITIDOS = Set.of(
            "image/jpeg", "image/png", "image/webp"
    );
    private static final Set<String> EXTENSIONES_PERMITIDAS = Set.of(
            ".jpg", ".jpeg", ".png", ".webp"
    );
    private static final long TAMANO_MAX_BYTES = 5 * 1024 * 1024; // 5 MB

    private final Path directorioBase;

    public AlmacenamientoFotosDisco(@Value("${woofmap.uploads.dir:uploads}") String directorioUploads) {
        this.directorioBase = Paths.get(directorioUploads).toAbsolutePath().normalize();
        inicializar();
    }

    private void inicializar() {
        try {
            Files.createDirectories(directorioBase);
        } catch (IOException e) {
            throw new StorageException("No se pudo crear el directorio de almacenamiento: " + directorioBase, e);
        }
    }

    @Override
    public String guardar(MultipartFile archivo) {
        validar(archivo);

        String extension = obtenerExtension(archivo.getOriginalFilename());
        String nombreArchivo = UUID.randomUUID() + extension;
        Path destino = directorioBase.resolve(nombreArchivo);

        try (InputStream entrada = archivo.getInputStream()) {
            Files.copy(entrada, destino, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            throw new StorageException("Error al guardar el archivo: " + nombreArchivo, e);
        }

        return "/uploads/" + nombreArchivo;
    }

    @Override
    public void eliminar(String rutaRelativa) {
        if (rutaRelativa == null || rutaRelativa.isBlank()) {
            return;
        }
        String nombreArchivo = rutaRelativa.startsWith("/uploads/")
                ? rutaRelativa.substring("/uploads/".length())
                : rutaRelativa;
        Path archivo = directorioBase.resolve(nombreArchivo).normalize();
        if (!archivo.startsWith(directorioBase)) {
            throw new StorageException("Ruta de archivo no permitida: " + rutaRelativa);
        }
        try {
            Files.deleteIfExists(archivo);
        } catch (IOException e) {
            throw new StorageException("Error al eliminar el archivo: " + nombreArchivo, e);
        }
    }

    // ── Validaciones ────────────────────────────────────────────────────────────

    private void validar(MultipartFile archivo) {
        if (archivo == null || archivo.isEmpty()) {
            throw new InvalidFileException("El archivo esta vacio o no se ha proporcionado");
        }

        if (archivo.getSize() > TAMANO_MAX_BYTES) {
            throw new InvalidFileException(
                    "El tamano del archivo supera el limite de 5 MB (recibido: " + archivo.getSize() + " bytes)");
        }

        String mime = archivo.getContentType();
        if (mime == null || !MIME_PERMITIDOS.contains(mime.toLowerCase())) {
            throw new InvalidFileException("Tipo de archivo no permitido: " + mime
                    + ". Se aceptan: image/jpeg, image/png, image/webp");
        }

        String extension = obtenerExtension(archivo.getOriginalFilename());
        if (!EXTENSIONES_PERMITIDAS.contains(extension.toLowerCase())) {
            throw new InvalidFileException("Extension de archivo no permitida: " + extension
                    + ". Se aceptan: .jpg, .jpeg, .png, .webp");
        }
    }

    private String obtenerExtension(String nombreOriginal) {
        if (nombreOriginal == null || !nombreOriginal.contains(".")) {
            return "";
        }
        return nombreOriginal.substring(nombreOriginal.lastIndexOf('.'));
    }
}
