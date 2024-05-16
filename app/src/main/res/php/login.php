<?php
$email = isset($_POST['email']) ? $_POST['email'] : '';
$password = isset($_POST['password']) ? $_POST['password'] : '';

$link = new mysqli('localhost', 'root', '', 'studystaydb');

if ($link->connect_error) {
    die('Error de Conexión: ' . $link->connect_error);
}

$stmt = $link->prepare("SELECT * FROM Users WHERE Email = ?");
$stmt->bind_param("s", $email);

if ($stmt->execute()) {
    $result = $stmt->get_result();
    $userFound = false;
    $output = array();
    while ($row = $result->fetch_assoc()) {
        if (password_verify($password, $row['Password'])) {
            $userFound = true;
            unset($row['Password']);
            $output[] = $row;
        }
    }

    if ($userFound) {
        // Inicio de sesión exitoso, devolver los datos del usuario en formato JSON
        echo json_encode($output);
    } else {
        // Autenticación fallida, devolver un mensaje de error en JSON
        echo json_encode(array("error" => "Usuario o contraseña inválidos."));
    }
} else {
    echo "Error en la ejecución de la consulta: " . $stmt->error;
}

$stmt->close();
$link->close();
?>