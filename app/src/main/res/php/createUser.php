<?php
$servername = "localhost";
$username = "root";
$password = "";
$dbname = "StudyStayDB";

$conn = new mysqli($servername, $username, $password, $dbname);

if ($conn->connect_error) {
    die("Conexión fallida: " . $conn->connect_error);
}

if ($_SERVER["REQUEST_METHOD"] == "POST") {
    $name = $_POST["name"];
    $lastName = $_POST["lastName"];
    $email = $_POST["email"];
    $password = $_POST["password"];
    $phone = $_POST["phone"];
    $birthDate = $_POST["birthDate"];
    $gender = $_POST["gender"];
    $dni = $_POST["dni"];

    $hashedPassword = password_hash($password, PASSWORD_DEFAULT);

    $sql = "INSERT INTO Users (Name, LastName, Email, Password, Phone, BirthDate, Gender, DNI) VALUES ('$name', '$lastName', '$email', '$hashedPassword', '$phone', '$birthDate', '$gender', '$dni')";

    if (mysqli_query($conn, $sql)) {
        echo "User created successfully";
    } else {
        echo "Error al crear el usuario: " . mysqli_error($conn);
    }
}

mysqli_close($conn);
?>
