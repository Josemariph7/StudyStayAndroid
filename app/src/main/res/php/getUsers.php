<?php
// Crear una nueva conexión mysqli
$link = new mysqli('localhost', 'root', '', 'usuarios');

// Verificar la conexión
if ($link->connect_error) {
    die(json_encode(array("error" => 'Error de Conexión: ' . $link->connect_error)));
}

// Preparar la consulta para obtener todos los usuarios
$query = "SELECT ID_USUARIO, USER, PASSWORD FROM usuarios";
$result = $link->query($query);

// Verificar si la consulta fue exitosa
if ($result) {
    $users = array();
    while ($row = $result->fetch_assoc()) {
        // Aquí podrías encriptar o manejar las contraseñas de manera segura antes de enviarlas
        $users[] = $row;
    }
    // Devolver el resultado en formato JSON
    echo json_encode($users);
} else {
    echo json_encode(array("error" => "Error en la ejecución de la consulta: " . $link->error));
}

// Cerrar la conexión
$link->close();
?>
