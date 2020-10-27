package com.springboot.reactor.apirest.apirest.handler;

import com.springboot.reactor.apirest.apirest.models.documents.Categoria;
import com.springboot.reactor.apirest.apirest.models.documents.Producto;
import com.springboot.reactor.apirest.apirest.models.services.ProductoServices;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.http.codec.multipart.FilePart;
import org.springframework.http.codec.multipart.FormFieldPart;
import org.springframework.stereotype.Component;
import org.springframework.validation.BeanPropertyBindingResult;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;
import org.springframework.web.reactive.function.BodyInserters;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.File;
import java.net.URI;
import java.util.Date;
import java.util.UUID;

@Component
public class ProductoHandler {

    @Autowired
    private ProductoServices productoServices;

    @Value("${config.uploads.path}")
    private String path;

    @Autowired
    private Validator validator;

    public Mono<ServerResponse> crearConFoto(ServerRequest serverRequest) {

        Mono<Producto> producto = serverRequest.multipartData().map(multiPart -> {
            FormFieldPart nombre = (FormFieldPart) multiPart.toSingleValueMap().get("nombre");
            FormFieldPart precio = (FormFieldPart) multiPart.toSingleValueMap().get("precio");
            FormFieldPart categoriaId = (FormFieldPart) multiPart.toSingleValueMap().get("categoria.id");
            FormFieldPart categoriaNombre = (FormFieldPart) multiPart.toSingleValueMap().get("categoria.nombre");

            Categoria categoria = new Categoria(categoriaNombre.value());
            categoria.setId(categoriaId.value());
            return new Producto(nombre.value(), Double.parseDouble(precio.value()), categoria);

        });

        return serverRequest.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(filePart -> producto
                        .flatMap(p -> {
                    p.setFoto(UUID.randomUUID().toString() + "-" + filePart.filename()
                            .replace(" ", "")
                            .replace(":", "")
                            .replace("\\", ""));
                    p.setCreateAt(new Date());
                    return filePart.transferTo(new File(path + p.getFoto())).then(productoServices.save(p));
                })).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(p)));
    }

    public Mono<ServerResponse> upload(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return serverRequest.multipartData().map(multipart -> multipart.toSingleValueMap().get("file"))
                .cast(FilePart.class)
                .flatMap(filePart -> productoServices.findById(id).flatMap(p -> {
                    p.setFoto(UUID.randomUUID().toString() + "-" + filePart.filename()
                            .replace(" ", "")
                            .replace(":", "")
                            .replace("\\", ""));
                    return filePart.transferTo(new File(path + p.getFoto())).then(productoServices.save(p));
                })).flatMap(p -> ServerResponse.created(URI.create("/api/v2/productos/".concat(p.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> listar(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoServices.findAll(), Producto.class);
    }

    public Mono<ServerResponse> ver(ServerRequest serverRequest) {
        String id = serverRequest.pathVariable("id");
        return productoServices.findById(id).flatMap(p -> ServerResponse
                .ok()
                .contentType(MediaType.APPLICATION_JSON)
                .body(BodyInserters.fromValue(p)))
                .switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> crear(ServerRequest request) {
        Mono<Producto> productoMono = request.bodyToMono(Producto.class);
        return productoMono.flatMap(p -> {

            Errors errors = new BeanPropertyBindingResult(p, Producto.class.getName());
            validator.validate(p, errors);

            if (errors.hasErrors()){
                return Flux.fromIterable(errors.getFieldErrors())
                        .map(fieldError -> "El campo " + fieldError.getField() + " " + fieldError.getDefaultMessage())
                        .collectList()
                        .flatMap(list -> ServerResponse.badRequest().body(BodyInserters.fromValue(list)));
            } else {
                if (p.getCreateAt() == null) {
                    p.setCreateAt(new Date());
                }
                return productoServices.save(p).flatMap(pdb -> ServerResponse
                        .created(URI.create("/api/v2/productos/".concat(pdb.getId())))
                        .contentType(MediaType.APPLICATION_JSON)
                        .body(BodyInserters.fromValue(pdb))
                );
            }
        });
    }

    public Mono<ServerResponse> editar(ServerRequest request) {
        Mono<Producto> productoMono = request.bodyToMono(Producto.class);
        String id = request.pathVariable("id");

        Mono<Producto> productoMonoDb = productoServices.findById(id);

        return productoMonoDb.zipWith(productoMono, (db, req) -> {
            db.setNombre(req.getNombre());
            db.setPrecio(req.getPrecio());
            db.setCategoria(req.getCategoria());
            return db;
        }).flatMap(p -> ServerResponse
                .created(URI.create("/api/v2/productos/".concat(p.getId())))
                .contentType(MediaType.APPLICATION_JSON)
                .body(productoServices.save(p), Producto.class)
        ).switchIfEmpty(ServerResponse.notFound().build());
    }

    public Mono<ServerResponse> eliminar(ServerRequest request) {
        String id = request.pathVariable("id");
        Mono<Producto> productoMonoDb = productoServices.findById(id);

        return productoMonoDb.flatMap(p -> productoServices
                .delete(p)
                .then(ServerResponse.noContent().build()))
                .switchIfEmpty(ServerResponse.notFound().build());

    }

}
