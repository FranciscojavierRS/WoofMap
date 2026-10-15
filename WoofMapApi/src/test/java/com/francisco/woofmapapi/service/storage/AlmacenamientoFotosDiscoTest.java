package com.francisco.woofmapapi.service.storage;

import com.francisco.woofmapapi.exception.InvalidFileException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AlmacenamientoFotosDiscoTest {

    @TempDir
    Path tempDir;

    private AlmacenamientoFotosDisco storageService;

    @BeforeEach
    void setUp() {
        storageService = new AlmacenamientoFotosDisco(tempDir.toString());
    }

    @Test
    @DisplayName("Debe guardar un archivo JPEG valido y devolver la ruta publica /uploads/...")
    void guardar_conJpegValido_guardaArchivoYDevuelveRuta() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "foto",
                "perro.jpg",
                "image/jpeg",
                "contenido-de-prueba-imagen".getBytes()
        );

        String ruta = storageService.guardar(file);

        assertThat(ruta).startsWith("/uploads/");
        assertThat(ruta).endsWith(".jpg");

        String nombreArchivo = ruta.replace("/uploads/", "");
        Path archivoEnDisco = tempDir.resolve(nombreArchivo);
        assertThat(Files.exists(archivoEnDisco)).isTrue();
        assertThat(Files.readAllBytes(archivoEnDisco)).isEqualTo("contenido-de-prueba-imagen".getBytes());
    }

    @Test
    @DisplayName("Debe guardar un archivo PNG valido")
    void guardar_conPngValido_guardaCorrectamente() {
        MockMultipartFile file = new MockMultipartFile(
                "foto",
                "parque.png",
                "image/png",
                "datos-png".getBytes()
        );

        String ruta = storageService.guardar(file);

        assertThat(ruta).startsWith("/uploads/");
        assertThat(ruta).endsWith(".png");
    }

    @Test
    @DisplayName("Debe lanzar InvalidFileException si el archivo es nulo o vacio")
    void guardar_archivoVacio_lanzaInvalidFileException() {
        MockMultipartFile file = new MockMultipartFile(
                "foto",
                "vacio.jpg",
                "image/jpeg",
                new byte[0]
        );

        assertThatThrownBy(() -> storageService.guardar(file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("vacio");

        assertThatThrownBy(() -> storageService.guardar(null))
                .isInstanceOf(InvalidFileException.class);
    }

    @Test
    @DisplayName("Debe lanzar InvalidFileException si el tipo MIME no es de imagen permitida")
    void guardar_tipoMimeNoPermitido_lanzaInvalidFileException() {
        MockMultipartFile file = new MockMultipartFile(
                "foto",
                "script.sh",
                "application/x-sh",
                "echo bad".getBytes()
        );

        assertThatThrownBy(() -> storageService.guardar(file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("Tipo de archivo no permitido");
    }

    @Test
    @DisplayName("Debe lanzar InvalidFileException si la extension del archivo no es valida")
    void guardar_extensionInvalida_lanzaInvalidFileException() {
        MockMultipartFile file = new MockMultipartFile(
                "foto",
                "virus.exe",
                "image/jpeg", // MIME trucado
                "contenido".getBytes()
        );

        assertThatThrownBy(() -> storageService.guardar(file))
                .isInstanceOf(InvalidFileException.class)
                .hasMessageContaining("Extension de archivo no permitida");
    }

    @Test
    @DisplayName("Debe eliminar el archivo si existe")
    void eliminar_archivoExistente_loElimina() throws IOException {
        Path archivo = tempDir.resolve("test-eliminar.jpg");
        Files.write(archivo, "datos".getBytes());
        assertThat(Files.exists(archivo)).isTrue();

        storageService.eliminar("/uploads/test-eliminar.jpg");

        assertThat(Files.exists(archivo)).isFalse();
    }
}
