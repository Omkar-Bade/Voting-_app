CREATE TABLE IF NOT EXISTS users (
    id INT NOT NULL AUTO_INCREMENT,
    first_name VARCHAR(50),
    last_name VARCHAR(50),
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    dob DATE,
    status TINYINT(1) DEFAULT 0,
    role VARCHAR(20) NOT NULL,
    PRIMARY KEY (id)
);

CREATE TABLE IF NOT EXISTS candidates (
    id INT NOT NULL AUTO_INCREMENT,
    name VARCHAR(50) NOT NULL,
    party VARCHAR(50) NOT NULL,
    votes INT DEFAULT 0,
    PRIMARY KEY (id)
);

-- Idempotent user seed data
INSERT INTO users (id, first_name, last_name, email, password, dob, status, role)
VALUES
(1, 'Rama','Kher','rama@gmail.com','ABQGCJaXOai58u+EtxWiug==:eRGCBluy4qiFM5S75dA1f5e49BTahAaWFaCIkOw7Cvo=','1999-01-01',0,'admin'),
(2, 'Shekhar','Patil','shekhar@gmail.com','ZH3bHCK0b2d+/mZYuAHTIg==:2XjBaweL+a9W98CiaR+iISWPJns4q8F+aqpVy8+7UAc=','1992-10-20',0,'voter'),
(3, 'Medha','Khole','medha@gmail.com','K/NVijmjNgVmlXCoUJsO7w==:nMiwanUkcE7V6wqj1EIxr/He5nQrvBVjkjNFTylegVw=','1990-11-21',0,'voter'),
(4, 'Anil','Ambani','anil@gmail.com','aakHgkVPM+5ZFk2780UGTA==:nRYjIXWJnH1diRLGQvYQnr8cUH7GYQlGynSUVS8pt/c=','1983-09-28',0,'voter'),
(5, 'Omkar','Bade','omkar@gmail.com','8XzJN2qPqNdoXGAKyPoEdQ==:rQY3rqgoPTXXagmw+4GyITaRzx87jv8a7NX4j8f0+h8=','2006-10-24',0,'voter')
ON DUPLICATE KEY UPDATE id=id;

-- Idempotent candidates seed data
INSERT INTO candidates (id, name, party, votes)
VALUES
(1, 'Ravi','bjp',0),
(2, 'Asha','ncp',0),
(3, 'Kiran','congress',0),
(4, 'Riya','sp',0),
(5, 'Subhash','aap',0),
(6, 'Pratik','bjp',0),
(7, 'Nirmal','congress',0),
(8, 'Sameer','bjp',0)
ON DUPLICATE KEY UPDATE id=id;
