package com.example.gestorcreditos;

import java.util.List;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // LEER: trae la lista de programas
    @GET("crud_programas.php")
    Call<List<ProgramaEducativo>> getProgramas();

    // CREAR: envía un objeto ProgramaEducativo en el cuerpo
    @POST("crud_programas.php")
    Call<ResponseBody> crearPrograma(@Body ProgramaEducativo programa);

    // ELIMINAR: envía el ID por la URL (crud_programas.php?id=5)
    @DELETE("crud_programas.php")
    Call<ResponseBody> eliminarPrograma(@Query("id") int id);
}
