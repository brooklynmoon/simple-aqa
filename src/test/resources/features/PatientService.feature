# language: ru
  @smoke
  @patients
  Функционал: patient=service

    @t07
    Сценарий: [patient-service] GET /patients
      * выполнен 'GET' запрос к URL 'http://localhost:4000/patients' и параметрами из таблицы и сохранен по ключу '13'
      * ответ '13' имеет статус-код '200'

    @t08
    Структура сценария: [patient-service] GET /patients/{id}
      * по ключу 'id' сохраняется значение '<ID>' с типом 'string'
      * выполнен 'GET' запрос к URL 'http://localhost:4000/patients/{id}' и параметрами из таблицы и сохранен по ключу '13'
      * ответ '13' имеет статус-код '200'

      Примеры:
        | ID                                   |
        | 9f4121e4-80b4-4d31-8bd6-6890bb5be7aa |
        | dfa8de08-1a75-4fef-a39b-cf6fcc32605f |

    @t04
    Сценарий: [patient-service] PUT /patient/{id}
      Дано по ключу 'name' сохраняется значение 'Another Name' с типом 'string'
      * по ключу 'id' сохраняется значение '223e4567-e89b-12d3-a456-426614174013' с типом 'string'
      * создан json-файл с параметрами из таблицы и сохранить по ключу 'jsonBody'
        | name    | {name}  |
      * выполнен 'PUT' запрос по 'http://localhost:4000/patients/{id}' и параметрами из таблицы и сохранен по ключу '13'
        | HEADER    | Content-Type    | application/json |
        | BODY      | {jsonBody}      |                  |
      * ответ '13' имеет статус-код '200'

    @t05
    Сценарий: [patient-service] PUT /patient/{id} [with email]
      Дано по ключу 'name' сохраняется значение 'Another NameIsNew' с типом 'string'
      * по ключу 'id' сохраняется значение '223e4567-e89b-12d3-a456-426614174013' с типом 'string'
      * создан json-файл с параметрами из таблицы и сохранить по ключу 'jsonBody'
        | name    | {name}            |
        | email   | newEmailAbcDefEDC@mail.com |
      * выполнен 'PUT' запрос по 'http://localhost:4000/patients/{id}' и параметрами из таблицы и сохранен по ключу '13'
        | HEADER    | Content-Type    | application/json |
        | BODY      | {jsonBody}      |                  |
      * ответ '13' имеет статус-код '200'

    @t06
    Сценарий: [patient-service] PUT /patient/{id} [email already exist – 400]
      Дано по ключу 'name' сохраняется значение 'Another Name' с типом 'string'
      * по ключу 'id' сохраняется значение '223e4567-e89b-12d3-a456-426614174013' с типом 'string'
      * создан json-файл с параметрами из таблицы и сохранить по ключу 'jsonBody'
        | name    | {name}                      |
        | email   | isabella.walker@example.com |
      * выполнен 'PUT' запрос по 'http://localhost:4000/patients/{id}' и параметрами из таблицы и сохранен по ключу '13'
        | HEADER    | Content-Type    | application/json |
        | BODY      | {jsonBody}      |                  |
      * ответ '13' имеет статус-код '400'