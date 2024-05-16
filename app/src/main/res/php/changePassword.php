<?php
// Obtener la nueva contraseña del parámetro POST o GET
$newPassword = isset($_POST['new_password']) ? $_POST['new_password'] : '';

// Validar la sesión del usuario (puedes agregar autenticación aquí)

if (!empty($newPassword)) {
    // Actualiza la contraseña en la base de datos (reemplaza 'usuarios' y 'user_id' por tus nombres de tabla y columna)
    $user_id = 1; // Cambia esto al ID del usuario que deseas actualizar
    $hashedPassword = password_hash($newPassword, PASSWORD_BCRYPT); // Hashea la nueva contraseña

    // Establece la conexión a tu base de datos
    $link = new mysqli('localhost', 'usuario', 'contrasena', 'nombre_base_de_datos');

    // Verificar la conexión
    if ($link->connect_error) {
        die('Error de Conexión: ' . $link->connect_error);
    }

    // Actualiza la contraseña en la base de datos
    $stmt = $link->prepare("UPDATE usuarios SET PASSWORD = ? WHERE ID_USUARIO = ?");
    $stmt->bind_param("si", $hashedPassword, $user_id);
    if ($stmt->execute()) {
        // Contraseña actualizada con éxito
        echo json_encode(array("message" => "Contraseña actualizada con éxito"));
    } else {
        echo json_encode(array("error" => "Error al actualizar la contraseña"));
    }

    // Cerrar la conexión
    $stmt->close();
    $link->close();
} else {
    echo json_encode(array("error" => "Nueva contraseña no proporcionada"));
}
?>
