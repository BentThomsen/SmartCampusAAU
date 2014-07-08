
-- --------------------------------------------------
-- Entity Designer DDL Script for SQL Server 2005, 2008, and Azure
-- --------------------------------------------------
-- Date Created: 05/14/2013 12:44:27
-- Generated from EDMX file: C:\Users\admin\Documents\Visual Studio 2010\Projects\RadioMapService3\RadioMapLocal\radiomap.edmx
-- --------------------------------------------------

SET QUOTED_IDENTIFIER OFF;
GO
USE [radiomap3];
GO
IF SCHEMA_ID(N'dbo') IS NULL EXECUTE(N'CREATE SCHEMA [dbo]');
GO

-- --------------------------------------------------
-- Dropping existing FOREIGN KEY constraints
-- --------------------------------------------------

IF OBJECT_ID(N'[dbo].[FK_VertexAbsoluteLocation]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[AbsoluteLocations] DROP CONSTRAINT [FK_VertexAbsoluteLocation];
GO
IF OBJECT_ID(N'[dbo].[FK_BuildingFloor1]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Building_Floors] DROP CONSTRAINT [FK_BuildingFloor1];
GO
IF OBJECT_ID(N'[dbo].[FK_BuildingBuilding_GpsBounds]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Building_GpsBounds] DROP CONSTRAINT [FK_BuildingBuilding_GpsBounds];
GO
IF OBJECT_ID(N'[dbo].[FK_BuildingMacInfo11]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Building_MacInfos] DROP CONSTRAINT [FK_BuildingMacInfo11];
GO
IF OBJECT_ID(N'[dbo].[FK_BuildingGraphicalMap11]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Building_Maps] DROP CONSTRAINT [FK_BuildingGraphicalMap11];
GO
IF OBJECT_ID(N'[dbo].[FK_BuildingEdge]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Edges] DROP CONSTRAINT [FK_BuildingEdge];
GO
IF OBJECT_ID(N'[dbo].[FK_BuildingVertex]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Vertices] DROP CONSTRAINT [FK_BuildingVertex];
GO
IF OBJECT_ID(N'[dbo].[FK_WifiMeasurementHistogram]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Histograms] DROP CONSTRAINT [FK_WifiMeasurementHistogram];
GO
IF OBJECT_ID(N'[dbo].[FK_VertexPixelLocation]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[PixelLocations] DROP CONSTRAINT [FK_VertexPixelLocation];
GO
IF OBJECT_ID(N'[dbo].[FK_VertexRelativeLocation]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[RelativeLocations] DROP CONSTRAINT [FK_VertexRelativeLocation];
GO
IF OBJECT_ID(N'[dbo].[FK_VertexSymbolicLocation]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[SymbolicLocations] DROP CONSTRAINT [FK_VertexSymbolicLocation];
GO
IF OBJECT_ID(N'[dbo].[FK_VertexWifiMeasurement]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[WifiMeasurements] DROP CONSTRAINT [FK_VertexWifiMeasurement];
GO
IF OBJECT_ID(N'[dbo].[FK_SnifferHistograms_SnifferWifiMeasurements]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[SnifferHistograms] DROP CONSTRAINT [FK_SnifferHistograms_SnifferWifiMeasurements];
GO
IF OBJECT_ID(N'[dbo].[FK_SnifferWifiMeasurements_Vertices]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[SnifferWifiMeasurements] DROP CONSTRAINT [FK_SnifferWifiMeasurements_Vertices];
GO
IF OBJECT_ID(N'[dbo].[FK_Vertex_Edges_Edge]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Vertex_Edges] DROP CONSTRAINT [FK_Vertex_Edges_Edge];
GO
IF OBJECT_ID(N'[dbo].[FK_Vertex_Edges_Vertex]', 'F') IS NOT NULL
    ALTER TABLE [dbo].[Vertex_Edges] DROP CONSTRAINT [FK_Vertex_Edges_Vertex];
GO

-- --------------------------------------------------
-- Dropping existing tables
-- --------------------------------------------------

IF OBJECT_ID(N'[dbo].[AbsoluteLocations]', 'U') IS NOT NULL
    DROP TABLE [dbo].[AbsoluteLocations];
GO
IF OBJECT_ID(N'[dbo].[Building_Floors]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Building_Floors];
GO
IF OBJECT_ID(N'[dbo].[Building_GpsBounds]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Building_GpsBounds];
GO
IF OBJECT_ID(N'[dbo].[Building_MacInfos]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Building_MacInfos];
GO
IF OBJECT_ID(N'[dbo].[Building_Maps]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Building_Maps];
GO
IF OBJECT_ID(N'[dbo].[Buildings]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Buildings];
GO
IF OBJECT_ID(N'[dbo].[Edges]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Edges];
GO
IF OBJECT_ID(N'[dbo].[Histograms]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Histograms];
GO
IF OBJECT_ID(N'[dbo].[PixelLocations]', 'U') IS NOT NULL
    DROP TABLE [dbo].[PixelLocations];
GO
IF OBJECT_ID(N'[dbo].[RelativeLocations]', 'U') IS NOT NULL
    DROP TABLE [dbo].[RelativeLocations];
GO
IF OBJECT_ID(N'[dbo].[SymbolicLocations]', 'U') IS NOT NULL
    DROP TABLE [dbo].[SymbolicLocations];
GO
IF OBJECT_ID(N'[dbo].[Vertices]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Vertices];
GO
IF OBJECT_ID(N'[dbo].[WifiMeasurements]', 'U') IS NOT NULL
    DROP TABLE [dbo].[WifiMeasurements];
GO
IF OBJECT_ID(N'[dbo].[SnifferHistograms]', 'U') IS NOT NULL
    DROP TABLE [dbo].[SnifferHistograms];
GO
IF OBJECT_ID(N'[dbo].[SnifferWifiMeasurements]', 'U') IS NOT NULL
    DROP TABLE [dbo].[SnifferWifiMeasurements];
GO
IF OBJECT_ID(N'[dbo].[Vertex_Edges]', 'U') IS NOT NULL
    DROP TABLE [dbo].[Vertex_Edges];
GO

-- --------------------------------------------------
-- Creating all tables
-- --------------------------------------------------

-- Creating table 'AbsoluteLocations'
CREATE TABLE [dbo].[AbsoluteLocations] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [latitude] decimal(18,14)  NULL,
    [longitude] decimal(18,14)  NULL,
    [altitude] decimal(18,14)  NULL,
    [Vertex_ID] int  NOT NULL
);
GO

-- Creating table 'Building_Floors'
CREATE TABLE [dbo].[Building_Floors] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [Number] int  NOT NULL,
    [Name] nvarchar(max)  NULL,
    [Building_ID] int  NOT NULL
);
GO

-- Creating table 'Building_GpsBounds'
CREATE TABLE [dbo].[Building_GpsBounds] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [Building_ID] int  NOT NULL,
    [bottomleft_lat] decimal(18,14)  NULL,
    [bottomleft_lon] decimal(18,14)  NULL,
    [bottomright_lat] decimal(18,14)  NULL,
    [bottomright_lon] decimal(18,14)  NULL,
    [topright_lat] decimal(18,14)  NULL,
    [topright_lon] decimal(18,14)  NULL,
    [topleft_lat] decimal(18,14)  NULL,
    [topleft_lon] decimal(18,14)  NULL
);
GO

-- Creating table 'Building_MacInfos'
CREATE TABLE [dbo].[Building_MacInfos] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [SSID] nvarchar(20)  NULL,
    [Building_ID] int  NOT NULL,
    [Mac] nvarchar(20)  NOT NULL
);
GO

-- Creating table 'Building_Maps'
CREATE TABLE [dbo].[Building_Maps] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [url] nvarchar(max)  NULL,
    [Building_ID] int  NOT NULL,
    [bottomleft_x] int  NULL,
    [bottomleft_y] int  NULL,
    [bottomright_x] int  NULL,
    [bottomright_y] int  NULL,
    [topleft_x] int  NULL,
    [topleft_y] int  NULL,
    [topright_x] int  NULL,
    [topright_y] int  NULL
);
GO

-- Creating table 'Buildings'
CREATE TABLE [dbo].[Buildings] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [Building_Name] nvarchar(50)  NULL,
    [Ifc_Url] nvarchar(200)  NULL,
    [Lat] decimal(18,14)  NULL,
    [Lon] decimal(18,14)  NULL,
    [Country] nvarchar(50)  NULL,
    [Postal_Code] nvarchar(50)  NULL,
    [Max_Address] nvarchar(255)  NULL,
    [Url] nvarchar(255)  NULL,
    [Sniffer_Url] nvarchar(max)  NULL
);
GO

-- Creating table 'Edges'
CREATE TABLE [dbo].[Edges] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [directional] bit  NOT NULL,
    [vertexOrigin] int  NOT NULL,
    [vertexDestination] int  NOT NULL,
    [Building_ID] int  NOT NULL,
    [is_stair] bit  NULL,
    [is_elevator] bit  NULL
);
GO

-- Creating table 'Histograms'
CREATE TABLE [dbo].[Histograms] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [value] int  NOT NULL,
    [count] int  NOT NULL,
    [WifiMeasurement_ID] int  NOT NULL,
    [Mac] nvarchar(max)  NOT NULL
);
GO

-- Creating table 'PixelLocations'
CREATE TABLE [dbo].[PixelLocations] (
    [ID] int  NOT NULL,
    [x] int  NULL,
    [y] int  NULL,
    [z] int  NULL,
    [Vertex_ID] int  NOT NULL
);
GO

-- Creating table 'RelativeLocations'
CREATE TABLE [dbo].[RelativeLocations] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [x] int  NULL,
    [y] int  NULL,
    [z] int  NULL,
    [Vertex_ID] int  NOT NULL
);
GO

-- Creating table 'SymbolicLocations'
CREATE TABLE [dbo].[SymbolicLocations] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [title] nvarchar(255)  NULL,
    [description] nvarchar(max)  NULL,
    [url] nvarchar(max)  NULL,
    [Vertex_ID] int  NOT NULL,
    [is_entrance] bit  NULL,
    [info_type] int  NULL
);
GO

-- Creating table 'Vertices'
CREATE TABLE [dbo].[Vertices] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [Building_ID] int  NOT NULL
);
GO

-- Creating table 'WifiMeasurements'
CREATE TABLE [dbo].[WifiMeasurements] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [meas_time_start] datetime  NULL,
    [meas_time_end] datetime  NULL,
    [Vertex_ID] int  NOT NULL,
    [Is_Collective] bit  NULL,
    [Was_Collective] bit  NULL
);
GO

-- Creating table 'SnifferHistograms'
CREATE TABLE [dbo].[SnifferHistograms] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [Mac] nvarchar(max)  NOT NULL,
    [value] int  NOT NULL,
    [count] int  NOT NULL,
    [WifiMeasurement_ID] int  NOT NULL
);
GO

-- Creating table 'SnifferWifiMeasurements'
CREATE TABLE [dbo].[SnifferWifiMeasurements] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [meas_time_start] datetime  NULL,
    [meas_time_end] datetime  NULL,
    [Vertex_ID] int  NOT NULL,
    [Is_Collective] bit  NULL,
    [Was_Collective] bit  NULL
);
GO

-- Creating table 'TrackedPositions'
CREATE TABLE [dbo].[TrackedPositions] (
    [ID] int IDENTITY(1,1) NOT NULL,
    [Building_ID] int  NULL,
    [VertexID] int  NULL,
    [Latitude] float  NOT NULL,
    [Longitude] float  NOT NULL,
    [Altitude] float  NOT NULL,
    [Provider] nvarchar(max)  NULL,
    [Time] datetime  NULL,
    [Accuracy] float  NULL,
    [Speed] float  NULL,
    [Bearing] float  NULL,
    [HasAccuracy] bit  NULL,
    [HasSpeed] bit  NULL,
    [HasBearing] bit  NULL,
    [ClientID] nvarchar(max)  NULL
);
GO

-- Creating table 'Vertex_Edges'
CREATE TABLE [dbo].[Vertex_Edges] (
    [Edges_ID] int  NOT NULL,
    [Vertices_ID] int  NOT NULL
);
GO

-- --------------------------------------------------
-- Creating all PRIMARY KEY constraints
-- --------------------------------------------------

-- Creating primary key on [ID] in table 'AbsoluteLocations'
ALTER TABLE [dbo].[AbsoluteLocations]
ADD CONSTRAINT [PK_AbsoluteLocations]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'Building_Floors'
ALTER TABLE [dbo].[Building_Floors]
ADD CONSTRAINT [PK_Building_Floors]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'Building_GpsBounds'
ALTER TABLE [dbo].[Building_GpsBounds]
ADD CONSTRAINT [PK_Building_GpsBounds]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID], [Mac] in table 'Building_MacInfos'
ALTER TABLE [dbo].[Building_MacInfos]
ADD CONSTRAINT [PK_Building_MacInfos]
    PRIMARY KEY CLUSTERED ([ID], [Mac] ASC);
GO

-- Creating primary key on [ID] in table 'Building_Maps'
ALTER TABLE [dbo].[Building_Maps]
ADD CONSTRAINT [PK_Building_Maps]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'Buildings'
ALTER TABLE [dbo].[Buildings]
ADD CONSTRAINT [PK_Buildings]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'Edges'
ALTER TABLE [dbo].[Edges]
ADD CONSTRAINT [PK_Edges]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'Histograms'
ALTER TABLE [dbo].[Histograms]
ADD CONSTRAINT [PK_Histograms]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'PixelLocations'
ALTER TABLE [dbo].[PixelLocations]
ADD CONSTRAINT [PK_PixelLocations]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'RelativeLocations'
ALTER TABLE [dbo].[RelativeLocations]
ADD CONSTRAINT [PK_RelativeLocations]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'SymbolicLocations'
ALTER TABLE [dbo].[SymbolicLocations]
ADD CONSTRAINT [PK_SymbolicLocations]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'Vertices'
ALTER TABLE [dbo].[Vertices]
ADD CONSTRAINT [PK_Vertices]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'WifiMeasurements'
ALTER TABLE [dbo].[WifiMeasurements]
ADD CONSTRAINT [PK_WifiMeasurements]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'SnifferHistograms'
ALTER TABLE [dbo].[SnifferHistograms]
ADD CONSTRAINT [PK_SnifferHistograms]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'SnifferWifiMeasurements'
ALTER TABLE [dbo].[SnifferWifiMeasurements]
ADD CONSTRAINT [PK_SnifferWifiMeasurements]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [ID] in table 'TrackedPositions'
ALTER TABLE [dbo].[TrackedPositions]
ADD CONSTRAINT [PK_TrackedPositions]
    PRIMARY KEY CLUSTERED ([ID] ASC);
GO

-- Creating primary key on [Edges_ID], [Vertices_ID] in table 'Vertex_Edges'
ALTER TABLE [dbo].[Vertex_Edges]
ADD CONSTRAINT [PK_Vertex_Edges]
    PRIMARY KEY NONCLUSTERED ([Edges_ID], [Vertices_ID] ASC);
GO

-- --------------------------------------------------
-- Creating all FOREIGN KEY constraints
-- --------------------------------------------------

-- Creating foreign key on [Vertex_ID] in table 'AbsoluteLocations'
ALTER TABLE [dbo].[AbsoluteLocations]
ADD CONSTRAINT [FK_VertexAbsoluteLocation]
    FOREIGN KEY ([Vertex_ID])
    REFERENCES [dbo].[Vertices]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_VertexAbsoluteLocation'
CREATE INDEX [IX_FK_VertexAbsoluteLocation]
ON [dbo].[AbsoluteLocations]
    ([Vertex_ID]);
GO

-- Creating foreign key on [Building_ID] in table 'Building_Floors'
ALTER TABLE [dbo].[Building_Floors]
ADD CONSTRAINT [FK_BuildingFloor1]
    FOREIGN KEY ([Building_ID])
    REFERENCES [dbo].[Buildings]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_BuildingFloor1'
CREATE INDEX [IX_FK_BuildingFloor1]
ON [dbo].[Building_Floors]
    ([Building_ID]);
GO

-- Creating foreign key on [Building_ID] in table 'Building_GpsBounds'
ALTER TABLE [dbo].[Building_GpsBounds]
ADD CONSTRAINT [FK_BuildingBuilding_GpsBounds]
    FOREIGN KEY ([Building_ID])
    REFERENCES [dbo].[Buildings]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_BuildingBuilding_GpsBounds'
CREATE INDEX [IX_FK_BuildingBuilding_GpsBounds]
ON [dbo].[Building_GpsBounds]
    ([Building_ID]);
GO

-- Creating foreign key on [Building_ID] in table 'Building_MacInfos'
ALTER TABLE [dbo].[Building_MacInfos]
ADD CONSTRAINT [FK_BuildingMacInfo11]
    FOREIGN KEY ([Building_ID])
    REFERENCES [dbo].[Buildings]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_BuildingMacInfo11'
CREATE INDEX [IX_FK_BuildingMacInfo11]
ON [dbo].[Building_MacInfos]
    ([Building_ID]);
GO

-- Creating foreign key on [Building_ID] in table 'Building_Maps'
ALTER TABLE [dbo].[Building_Maps]
ADD CONSTRAINT [FK_BuildingGraphicalMap11]
    FOREIGN KEY ([Building_ID])
    REFERENCES [dbo].[Buildings]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_BuildingGraphicalMap11'
CREATE INDEX [IX_FK_BuildingGraphicalMap11]
ON [dbo].[Building_Maps]
    ([Building_ID]);
GO

-- Creating foreign key on [Building_ID] in table 'Edges'
ALTER TABLE [dbo].[Edges]
ADD CONSTRAINT [FK_BuildingEdge]
    FOREIGN KEY ([Building_ID])
    REFERENCES [dbo].[Buildings]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_BuildingEdge'
CREATE INDEX [IX_FK_BuildingEdge]
ON [dbo].[Edges]
    ([Building_ID]);
GO

-- Creating foreign key on [Building_ID] in table 'Vertices'
ALTER TABLE [dbo].[Vertices]
ADD CONSTRAINT [FK_BuildingVertex]
    FOREIGN KEY ([Building_ID])
    REFERENCES [dbo].[Buildings]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_BuildingVertex'
CREATE INDEX [IX_FK_BuildingVertex]
ON [dbo].[Vertices]
    ([Building_ID]);
GO

-- Creating foreign key on [WifiMeasurement_ID] in table 'Histograms'
ALTER TABLE [dbo].[Histograms]
ADD CONSTRAINT [FK_WifiMeasurementHistogram]
    FOREIGN KEY ([WifiMeasurement_ID])
    REFERENCES [dbo].[WifiMeasurements]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_WifiMeasurementHistogram'
CREATE INDEX [IX_FK_WifiMeasurementHistogram]
ON [dbo].[Histograms]
    ([WifiMeasurement_ID]);
GO

-- Creating foreign key on [Vertex_ID] in table 'PixelLocations'
ALTER TABLE [dbo].[PixelLocations]
ADD CONSTRAINT [FK_VertexPixelLocation]
    FOREIGN KEY ([Vertex_ID])
    REFERENCES [dbo].[Vertices]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_VertexPixelLocation'
CREATE INDEX [IX_FK_VertexPixelLocation]
ON [dbo].[PixelLocations]
    ([Vertex_ID]);
GO

-- Creating foreign key on [Vertex_ID] in table 'RelativeLocations'
ALTER TABLE [dbo].[RelativeLocations]
ADD CONSTRAINT [FK_VertexRelativeLocation]
    FOREIGN KEY ([Vertex_ID])
    REFERENCES [dbo].[Vertices]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_VertexRelativeLocation'
CREATE INDEX [IX_FK_VertexRelativeLocation]
ON [dbo].[RelativeLocations]
    ([Vertex_ID]);
GO

-- Creating foreign key on [Vertex_ID] in table 'SymbolicLocations'
ALTER TABLE [dbo].[SymbolicLocations]
ADD CONSTRAINT [FK_VertexSymbolicLocation]
    FOREIGN KEY ([Vertex_ID])
    REFERENCES [dbo].[Vertices]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_VertexSymbolicLocation'
CREATE INDEX [IX_FK_VertexSymbolicLocation]
ON [dbo].[SymbolicLocations]
    ([Vertex_ID]);
GO

-- Creating foreign key on [Vertex_ID] in table 'WifiMeasurements'
ALTER TABLE [dbo].[WifiMeasurements]
ADD CONSTRAINT [FK_VertexWifiMeasurement]
    FOREIGN KEY ([Vertex_ID])
    REFERENCES [dbo].[Vertices]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_VertexWifiMeasurement'
CREATE INDEX [IX_FK_VertexWifiMeasurement]
ON [dbo].[WifiMeasurements]
    ([Vertex_ID]);
GO

-- Creating foreign key on [WifiMeasurement_ID] in table 'SnifferHistograms'
ALTER TABLE [dbo].[SnifferHistograms]
ADD CONSTRAINT [FK_SnifferHistograms_SnifferWifiMeasurements]
    FOREIGN KEY ([WifiMeasurement_ID])
    REFERENCES [dbo].[SnifferWifiMeasurements]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_SnifferHistograms_SnifferWifiMeasurements'
CREATE INDEX [IX_FK_SnifferHistograms_SnifferWifiMeasurements]
ON [dbo].[SnifferHistograms]
    ([WifiMeasurement_ID]);
GO

-- Creating foreign key on [Vertex_ID] in table 'SnifferWifiMeasurements'
ALTER TABLE [dbo].[SnifferWifiMeasurements]
ADD CONSTRAINT [FK_SnifferWifiMeasurements_Vertices]
    FOREIGN KEY ([Vertex_ID])
    REFERENCES [dbo].[Vertices]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_SnifferWifiMeasurements_Vertices'
CREATE INDEX [IX_FK_SnifferWifiMeasurements_Vertices]
ON [dbo].[SnifferWifiMeasurements]
    ([Vertex_ID]);
GO

-- Creating foreign key on [Edges_ID] in table 'Vertex_Edges'
ALTER TABLE [dbo].[Vertex_Edges]
ADD CONSTRAINT [FK_Vertex_Edges_Edge]
    FOREIGN KEY ([Edges_ID])
    REFERENCES [dbo].[Edges]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;
GO

-- Creating foreign key on [Vertices_ID] in table 'Vertex_Edges'
ALTER TABLE [dbo].[Vertex_Edges]
ADD CONSTRAINT [FK_Vertex_Edges_Vertex]
    FOREIGN KEY ([Vertices_ID])
    REFERENCES [dbo].[Vertices]
        ([ID])
    ON DELETE NO ACTION ON UPDATE NO ACTION;

-- Creating non-clustered index for FOREIGN KEY 'FK_Vertex_Edges_Vertex'
CREATE INDEX [IX_FK_Vertex_Edges_Vertex]
ON [dbo].[Vertex_Edges]
    ([Vertices_ID]);
GO

-- --------------------------------------------------
-- Script has ended
-- --------------------------------------------------