package com.francisco.woofmapapi.service.storage;

import org.springframework.web.multipart.MultipartFile;

public interface AlmacenamientoFotos {

    /**
     * Almacena un archivo de imagen en el soporte configurado (disco, MinIO, etc.).
     *
     * @param archivo archivo recibido en la peticion multipart
     * @return URL o ruta publica relativa del archivo almacenado
     */
    String guardar(MultipartFile archivo);

    /**
     * Elimina un archivo previamente almacenado.
     *
     * @param rutaRelativa ruta o nombre del archivo a eliminar
     */
    void eliminar(String rutaRelativa);
}
